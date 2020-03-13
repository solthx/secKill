package com.czf.service;

import com.czf.service.model.PromoModel;

/**
 * @author czf
 * @Date 2020/3/13 4:50 下午
 */
public interface PromoService {
    PromoModel getPromoByItemId(Integer itemId);
}
