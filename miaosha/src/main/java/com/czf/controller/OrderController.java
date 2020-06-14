package com.czf.controller;

import com.czf.error.BusinessException;
import com.czf.error.EmBusinessError;
import com.czf.mq.MqProducer;
import com.czf.response.CommonReturnType;
import com.czf.service.ItemService;
import com.czf.service.OrderService;
import com.czf.service.PromoService;
import com.czf.service.model.OrderModel;
import com.czf.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author czf
 * @Date 2020/3/12 3:40 下午
 */

@Controller("order_controller")
@RequestMapping("/order")
@CrossOrigin(origins = {"*"}, allowCredentials = "true")
public class OrderController extends BaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MqProducer mqProducer;

    @Autowired
    private ItemService itemService;

    @Autowired
    private PromoService promoService;

    // 生成秒杀令牌
    @RequestMapping(value = "/generatetoken", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType generateToken(@RequestParam("itemId")Integer itemId,
                                        @RequestParam("promoId")Integer promoId) throws BusinessException {
        // 根据token获取用户信息
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token))
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户仍未登陆, 请登陆后下单");
        UserModel userModel = (UserModel)redisTemplate.opsForValue().get(token);
        if (userModel==null)
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户仍未登陆, 请登陆后下单");
        // 获取秒杀访问令牌
        String promoToken = promoService.generateSecondKillToken(promoId, itemId,userModel.getUid());
        // 返回对应结果
        if (promoToken==null)
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"生成令牌失败");
        return CommonReturnType.create(null);
    }

        // 封装下单请求
    @RequestMapping(value = "/createorder", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam("itemId")Integer itemId,
                                        @RequestParam(value = "promoId",required = false)Integer promoId,
                                        @RequestParam(value = "amount")Integer amount,
                                        @RequestParam(value = "promoToken",required = false)String promoToken) throws BusinessException {

//        throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户仍未登陆, 请登陆后下单");
//        Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
//        if ( isLogin==null || isLogin==false ) {
//            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户仍未登陆, 请登陆后下单");
//        }
//        UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");

        // 获取用户的登陆信息 , 使用token的方式

        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token))
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户仍未登陆, 请登陆后下单");
        UserModel userModel = (UserModel)redisTemplate.opsForValue().get(token);
        if (userModel==null)
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户仍未登陆, 请登陆后下单");
        if (promoId!=null) {
            String inRedisPromoToken = (String) redisTemplate.opsForValue().get("promo_token_"+promoId+"_userId_"+userModel.getUid()+"_itemId_"+itemId);

            if ( inRedisPromoToken==null /*||  !StringUtils.equals(promoToken, inRedisPromoToken)*/){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"秒杀令牌校验失败");
            }
        }
        //OrderModel orderModel = orderService.createOrder(userModel.getUid(),promoId, itemId, amount);

        // 判断是否售罄
        if (redisTemplate.hasKey("promo_item_stock_invalid_"+itemId))
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);

        // 加入库存流水init状态
        String stockLogId = itemService.initStockLog(itemId, amount);

        // 再去完成对应的"下单"事务型消息
        if (!mqProducer.transactionAsyncReduceStock(itemId,amount,promoId,userModel.getUid(), stockLogId))
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR,"下单失败!");

        return CommonReturnType.create(null);
    }
}
