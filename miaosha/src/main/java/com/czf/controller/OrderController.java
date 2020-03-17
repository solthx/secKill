package com.czf.controller;

import com.czf.error.BusinessException;
import com.czf.error.EmBusinessError;
import com.czf.response.CommonReturnType;
import com.czf.service.OrderService;
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

    // 封装下单请求
    @RequestMapping(value = "createorder", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam("itemId")Integer itemId,
                                        @RequestParam("promoId")Integer promoId,
                                        @RequestParam("amount")Integer amount) throws BusinessException {

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

        OrderModel orderModel = orderService.createOrder(userModel.getUid(),promoId, itemId, amount);
        return CommonReturnType.create(orderModel);
    }
}
