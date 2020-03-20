package com.czf.controller;

import com.czf.controller.viewobject.ItemVO;
import com.czf.error.BusinessException;
import com.czf.response.CommonReturnType;
import com.czf.service.CacheService;
import com.czf.service.ItemService;
import com.czf.service.model.ItemModel;
import com.czf.service.model.PromoModel;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author czf
 * @Date 2020/3/11 12:48 上午
 */

@Controller("item_controller")
@RequestMapping("/item")
@CrossOrigin(origins = {"*"}, allowCredentials = "true")
public class ItemController extends BaseController {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 创建商品
     * @param title
     * @param description
     * @param price
     * @param stock
     * @param imgUrl
     * @return
     */
    @RequestMapping(value = "/create", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createItem(@RequestParam(name="title")String title,
                                       @RequestParam(name="description")String description,
                                       @RequestParam(name="price") BigDecimal price,
                                       @RequestParam(name="stock")Integer stock,
                                       @RequestParam(name="imgUrl")String imgUrl) throws BusinessException {
        // 封装service请求用来创建商品
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setStock(stock);
        itemModel.setPrice(price);
        itemModel.setDescription(description);
        itemModel.setImgUrl(imgUrl);
        ItemModel itemModelForReturn = itemService.createItem(itemModel);
        // 创建View返回给前端
        ItemVO itemVO = convertVOFromModel(itemModelForReturn);
        return CommonReturnType.create(itemVO);
    }

    private ItemVO convertVOFromModel(ItemModel itemModel){
        if (itemModel == null)
            return null;
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel, itemVO);
        if (itemModel.getPromoModel()!=null) {
            PromoModel promoModel = itemModel.getPromoModel();
            itemVO.setPromoStatus(promoModel.getStatus());
            itemVO.setPromoId(promoModel.getId());
            itemVO.setStartDate(promoModel.getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
            itemVO.setPromoPrice(promoModel.getPromoItemPrice());
        }else
            itemVO.setPromoStatus(0);
        return itemVO;
    }

    /**
     * 商品详情浏览页面
     * get幂等操作
     * @param id
     * @return
     */
    @RequestMapping(value = "/get", method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType getItem(@RequestParam("id")Integer id){
        String itemKey = "item_"+id;
        // 先在本地缓存取
        ItemModel itemModel = (ItemModel) cacheService.getCommonCache(itemKey);
        if (itemModel==null){
            // 本地缓存不存在, 取redis中取
            itemModel = (ItemModel) redisTemplate.opsForValue().get(itemKey);
            if (itemModel==null) {
                // redis缓存也不存在，去数据库中取
                itemModel = itemService.getItemById(id);
                redisTemplate.opsForValue().set(itemKey, itemModel);
                redisTemplate.expire(itemKey, 10, TimeUnit.MINUTES);
            }
            cacheService.setCommonCache(itemKey,itemModel);
        }
        ItemVO itemVO = convertVOFromModel(itemModel);
        return CommonReturnType.create(itemVO);
    }

    /**
     * 商品列表浏览
     * @return
     */
    @RequestMapping(value = "/list", method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType listItem(){
        List<ItemModel> itemModelList = itemService.listItems();
        // 使用stream API将itemModelList转化成itemVOList
        List<ItemVO> itemVOList = itemModelList.stream().map(itemModel -> {
            return convertVOFromModel(itemModel);
        }).collect(Collectors.toList());
        return CommonReturnType.create(itemVOList);
    }
}
