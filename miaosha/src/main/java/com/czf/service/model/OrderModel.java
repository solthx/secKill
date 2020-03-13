package com.czf.service.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author czf
 * @Date 2020/3/12 11:07 上午
 */
@Data
public class OrderModel {
    private String id; // 订单号

    private Integer userId; // 下单用户

    private Integer itemId; // 下单商品Id

    private Integer amount; // 购买数量

    private BigDecimal itemPrice; // 下单时的商品价格 ， 若promoId非空，则为秒杀商品的价格

    private BigDecimal orderPrice; // 购买金额， 若promoId非空，则为秒杀商品的价格

    private Integer promoId; // 若非空，则表示以秒杀商品的方式下单

}
