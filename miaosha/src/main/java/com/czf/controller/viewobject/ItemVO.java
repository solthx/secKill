package com.czf.controller.viewobject;

import lombok.Data;
import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * @author czf
 * @Date 2020/3/11 12:48 上午
 */
@Data
public class ItemVO {
    // 商品id
    private Integer id;

    // 商品名称
    private String title;

    // 商品价格
    // Double传到前端会出现精度问题，因此这里使用BigDecimal
    private BigDecimal price;

    // 商品库存
    private Integer stock;

    // 商品销量
    private Integer sales;

    // 商品描述
    private String description;

    // 商品图片的url
    private String imgUrl;

    // 0：没有秒杀活动，1表示秒杀活动待开始，2表示秒杀活动进行中
    private Integer promoStatus;

    // 秒杀活动价格
    private BigDecimal promoPrice;

    // 秒杀商品ID
    private Integer promoId;

    // 秒杀活动开始时间，用于倒计时
    private String startDate;
}
