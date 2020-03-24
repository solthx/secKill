package com.czf.service;

import com.czf.error.BusinessException;
import com.czf.service.model.ItemModel;

import java.util.List;

/**
 * @author czf
 * @Date 2020/3/10 11:12 下午
 */
public interface ItemService {
    /**
     * 创建商品
     * @param itemModel
     * @return
     */
    ItemModel createItem(ItemModel itemModel) throws BusinessException;

    /**
     * 返回所有商品的列表
     * @return
     */
    List<ItemModel> listItems();

    /**
     * 根据商品id查找指定商品
     * @param id
     * @return
     */
    ItemModel getItemById(Integer id);

    /**
     * 扣减库存第一步：从缓存中扣减库存
     *
     * ps: 真正的库存扣减被分为如下两步：
     *      1. 从缓存中扣减库存
     *      2. 从数据库中扣减库存 (由消息队列通知
     *
     * @param itemId
     * @return
     */
    boolean decreaseStockInCache(Integer itemId, Integer amount);

    /**
     * 扣减库存第二步：发送message，异步扣减库存进数据库
     * @param itemId
     * @param amount
     * @return
     */
    boolean asyncDecreaseStock(Integer itemId, Integer amount);

    /**
     * 回滚decreaseStockInCache
     * @param itemId
     * @param amount
     * @return
     */
    boolean increaseStockInCache(Integer itemId, Integer amount);

    /**
     * 销量增加
     * @param itemId
     * @param amount
     * @return
     */
    int increaseSales(Integer itemId, Integer amount);

    /**
     * 根据Id尝试从Redis中获取Item
     * @param id
     * @return
     */
    ItemModel getItemByIdInCache(Integer id);

    /**
     * 初始化库存流水
     * @param itemId
     * @param amount
     */
    String initStockLog(Integer itemId, Integer amount);
}
