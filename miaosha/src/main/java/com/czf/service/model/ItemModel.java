package com.czf.service.model;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author czf
 * @Date 2020/3/10 10:16 下午
 */
@Data
public class ItemModel {
    // 商品id
    private Integer id;

    // 商品名称
    @NotBlank(message = "商品名称不能为空")
    private String title;

    // 商品价格
    // Double传到前端会出现精度问题，因此这里使用BigDecimal
    @NotNull(message = "商品价格不能为空")
    @Min(value = 0,message = "商品价格必须大于0")
    private BigDecimal price;

    // 商品库存
    @NotNull(message = "商品不能为空")
    private Integer stock;

    // 商品销量
    private Integer sales;

    // 商品描述
    @NotBlank(message = "商品描述不能为空")
    private String description;

    // 商品图片的url
    @NotBlank(message = "图片信息不能为空")
    private String imgUrl;

    // 使用聚合模型
    // 如果promoModel不是空，则表示其拥有还未结束的秒杀活动
    private PromoModel promoModel;
}
