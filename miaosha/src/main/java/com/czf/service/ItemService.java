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
}
