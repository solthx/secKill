package com.czf.service;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 封装本地缓存操作类
 * @author czf
 * @Date 2020/3/19 2:28 下午
 */
public interface CacheService {
    // 存方法
    void setCommonCache(String key, Object object);

    // 取方法
    Object getCommonCache(String key);
}
