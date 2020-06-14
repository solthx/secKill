package com.czf.service.impl;

import com.czf.dao.OrderDOMapper;
import com.czf.dao.SequenceDOMapper;
import com.czf.dao.StockLogDOMapper;
import com.czf.dataobject.OrderDO;
import com.czf.dataobject.SequenceDO;
import com.czf.dataobject.StockLogDO;
import com.czf.error.BusinessException;
import com.czf.error.EmBusinessError;
import com.czf.service.ItemService;
import com.czf.service.OrderService;
import com.czf.service.UserService;
import com.czf.service.model.ItemModel;
import com.czf.service.model.OrderModel;
import com.czf.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author czf
 * @Date 2020/3/12 12:52 下午
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Autowired
    private StockLogDOMapper stockLogDOMapper;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId,Integer promoId, Integer itemId, Integer amount, String stockLogId) throws BusinessException {
        /**
         * 1. 检验下单状态：
         *      1. 用户是否合法
         *      2. 商品是否存在
         *      3. 购买数量是否合法
         */

        //ItemModel itemModel = itemService.getItemById(itemId);
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if ( itemModel==null )
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "商品信息不存在!");
//
//        UserModel userModel = userService.getUserById(userId);
//        if ( userModel==null )
//            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "用户不存在!");

        if ( amount<=0 || amount>99 )
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "数量信息不正确！");
        // 校验活动信息
//        if ( promoId!=null ){
//            // (1) 校验对应活动是否存在这个适用商品
//            if ( promoId != itemModel.getPromoModel().getId() )
//                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"活动信息不正确！");
//            else if ( itemModel.getPromoModel().getStatus()!=2 )
//                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"活动尚未开始！");
//
//        }

        /**
         * 2. 落单减库存
         */

        boolean result = itemService.decreaseStockInCache(itemId,amount);
        if ( !result )
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);

        /**
         * 3. 订单入库
         */

        // 生成orderModel
        OrderModel orderModel = new OrderModel();
        orderModel.setAmount(amount);
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);

        if (promoId==null)
            orderModel.setItemPrice(itemModel.getPrice());
        else orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());

        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));
        orderModel.setPromoId(promoId);
        // 生成交易流水号, 订单号
        orderModel.setId(generateOrderNo());

        // model -> DO
        OrderDO orderDO = converFromOrderModel(orderModel);

        // 存入订单到数据库
        orderDOMapper.insertSelective(orderDO);

        // 增加销量
        int sav = itemService.increaseSales(itemId, amount);

        // 设置库存流水状态为成功
        StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
        if (stockLogDO==null)
            throw  new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        stockLogDO.setStatus(2);
        stockLogDOMapper.updateByPrimaryKeySelective(stockLogDO);


//        boolean mqResult = itemService.asyncDecreaseStock(itemId, amount);
//        if (!mqResult){
//            // 回滚库存
//            itemService.increaseStockInCache(itemId, amount);
//            throw new BusinessException(EmBusinessError.MQ_SEND_ERROR);
//        }

        /**
         * 4. 返回给前前端
         */
        return orderModel;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    String generateOrderNo(){
        StringBuilder stringBuilder = new StringBuilder();
        // 订单号16位
        // 前8位为时间信息，年月日
        LocalDateTime now = LocalDateTime.now();
        stringBuilder.append(now.format(DateTimeFormatter.ISO_DATE).replace("-",""));
        // 中间6位为自增序列

        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        int sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue()+sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);

        String sequenceStr = String.valueOf(sequence);
        for(int i=0; i<6-sequenceStr.length(); ++i)
            stringBuilder.append(0);
        stringBuilder.append(sequenceStr);

        // 最后2位为分库分表位
        stringBuilder.append("00"); // 暂时写死00，日后讨论
        return stringBuilder.toString();
    }

    private OrderDO converFromOrderModel(OrderModel orderModel){
        if ( orderModel==null )
            return null;
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel, orderDO);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDO;
    }
}
