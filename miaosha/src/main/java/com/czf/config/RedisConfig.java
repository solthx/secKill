package com.czf.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @author czf
 * @Date 2020/3/17 11:29 上午
 */
@Configuration
@EnableRedisHttpSession
public class RedisConfig {
}
