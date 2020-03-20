package com.czf.service.impl;

import com.czf.service.CacheService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @author czf
 * @Date 2020/3/19 2:29 下午
 */
@Service
public class CacheServiceImpl implements CacheService {
    private Cache<String, Object> commonCache = null;


    /**
     * 被@PostConstruct修饰的方法会在服务器加载Servlet的时候运行，
     * 并且只会被服务器执行一次。PostConstruct在构造函数之后执行，
     * init（）方法之前执行。PreDestroy（）方法在destroy（）方法知性之后执行
     */
    @PostConstruct
    public void init(){
        commonCache = CacheBuilder.newBuilder()
                // 设置缓存的初始容量为10
                .initialCapacity(10)
                // 设置缓存中最大可以存储100个KEY，超过100个后，会按照LRU的策略移除
                .maximumSize(100)
                //设置写缓存后多少秒过期
                .expireAfterWrite(30, TimeUnit.SECONDS).build();
    }

    @Override
    public void setCommonCache(String key, Object object) {
        commonCache.put(key,object);
    }

    @Override
    public Object getCommonCache(String key) {
        return commonCache.getIfPresent(key); // 存在就返回，不存在就返回null
    }
}
