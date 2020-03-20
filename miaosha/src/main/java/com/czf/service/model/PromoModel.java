package com.czf.service.model;

import lombok.Data;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author czf
 * @Date 2020/3/12 11:44 下午
 */
@Data
public class PromoModel implements Serializable {
    private Integer id;

    // 秒杀活动名称
    private String promoName;

    // 秒杀活动的开始时间
    private DateTime startDate;

    // 秒杀活动的结束时间
    private DateTime endDate;

    // 秒杀活动的适用商品
    private  Integer itemId;

    // 秒杀活动的商品价格
    private BigDecimal promoItemPrice;

    // 秒杀活动状态
    // 1：未开始， 2：进行中，3：已结束
    private Integer status;
}
