package com.czf.service;

import com.czf.service.model.PromoModel;

/**
 * @author czf
 * @Date 2020/3/13 4:50 下午
 */
public interface PromoService {
    /**
     * 根据ItemId获取即将进行或正在进行的秒杀活动
     * @param itemId
     * @return
     */
    PromoModel getPromoByItemId(Integer itemId);

    /**
     * 活动发布
     * @param promoId
     */
    void publishPromo(Integer promoId);
}
