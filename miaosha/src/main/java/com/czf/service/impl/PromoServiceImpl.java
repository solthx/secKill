package com.czf.service.impl;

import com.czf.dao.PromoDOMapper;
import com.czf.dataobject.PromoDO;
import com.czf.error.BusinessException;
import com.czf.error.EmBusinessError;
import com.czf.service.ItemService;
import com.czf.service.PromoService;
import com.czf.service.UserService;
import com.czf.service.model.ItemModel;
import com.czf.service.model.PromoModel;
import com.czf.service.model.UserModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author czf
 * @Date 2020/3/13 4:51 下午
 */
@Service
public class PromoServiceImpl implements PromoService {
    @Autowired
    private PromoDOMapper promoDOMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据ItemId获取即将进行的，或正在进行的秒杀活动
     * @param itemId
     * @return
     */
    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        // 获取对应商品的秒杀活动信息
        PromoDO promoDO =  promoDOMapper.selectByItemId(itemId);

        if (promoDO==null)
            return null;

        // dao -> model
        PromoModel promoModel = convertFromDataObject(promoDO);


        // 判断当前时间是否秒杀活动即将开始或正在进行

        if (promoModel.getStartDate().isAfterNow())
            promoModel.setStatus(1); // 未开始
        else if (promoModel.getEndDate().isBeforeNow())
            promoModel.setStatus(3); // 已结束
        else promoModel.setStatus(2); // 进行中
        return promoModel;
    }

    /**
     * 活动发布
     *
     * @param promoId
     */
    @Override
    public void publishPromo(Integer promoId) {
        // 通过活动Id获取活动
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        // 活动是否存在
        if (promoDO==null || promoDO.getItemId()==null || promoDO.getItemId().intValue()==0)
            return;

        // 这里假设，在秒杀开始前的一段时间内，商品不再对外售卖，可以在这个时间段内发布促销商品
        // 因此，这里更新缓存里的库存将不会影响正常售卖
        ItemModel itemModel = itemService.getItemById(promoDO.getItemId());
        /// 发布到redis中
        redisTemplate.opsForValue().set("promo_item_stock_"+itemModel.getId(), itemModel.getStock());
    }

    /**
     * 生成秒杀用的令牌
     *
     * @param promoId
     */
    @Override
    public String generateSecondKillToken(Integer promoId,Integer itemId, Integer userId) {
        // 获取对应商品的秒杀活动信息
        PromoDO promoDO =  promoDOMapper.selectByPrimaryKey(promoId);

        if (promoDO==null)
            return null;

        // dao -> model
        PromoModel promoModel = convertFromDataObject(promoDO);

        // 判断当前时间是否秒杀活动即将开始或正在进行

        if (promoModel.getStartDate().isAfterNow())
            promoModel.setStatus(1); // 未开始
        else if (promoModel.getEndDate().isBeforeNow())
            promoModel.setStatus(3); // 已结束
        else promoModel.setStatus(2); // 进行中

        // 活动不在进行中
        if (promoModel.getStatus().intValue()!=2){
            return null;
        }

        // 判断item促销信息是否存在
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if (itemModel==null)
            return null;

        // 判断用户是否存在
        UserModel userModel = userService.getUserByIdInCache(userId);
        if (userModel==null)
            return null;

        // 生成token令牌，并存入redis，给一个5分钟的有效期
        String token = UUID.randomUUID().toString().replace("-","");
        redisTemplate.opsForValue().set("promo_token_"+promoId+"_userId_"+userId+"_itemId_"+itemId, token);
        redisTemplate.expire("promo_token_"+promoId+"_userId_"+userId+"_itemId_"+itemId, 5, TimeUnit.MINUTES);
        return token;
    }

    private PromoModel convertFromDataObject(PromoDO promoDO){
        if (promoDO==null)
            return  null;
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO, promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));
        return promoModel;
    }
}
