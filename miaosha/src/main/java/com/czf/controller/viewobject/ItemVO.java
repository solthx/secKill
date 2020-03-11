package com.czf.controller.viewobject;

import lombok.Data;

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
}
