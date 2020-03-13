package com.czf.service;

import com.czf.controller.viewobject.ItemVO;
import com.czf.error.BusinessException;
import com.czf.service.model.OrderModel;

/**
 * @author czf
 * @Date 2020/3/12 12:51 下午
 */
public interface OrderService {
    // 推荐 方式一: 通过前端url上传过来秒杀活动id, 然后下单接口内校验对应id是否属于对应商品且活动已开启
    // 不推荐：方式二: 直接在下单接口内哦按段对应的商品是否存在秒杀活动，若存在进行中的则以秒杀价格下单
    OrderModel createOrder(Integer userId, Integer promoId, Integer itemId, Integer amount) throws BusinessException;


}
