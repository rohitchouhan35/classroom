package com.rohitchouhan.classroom.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisService {

    @Autowired
    private RedisTemplate redisTemplate;

    private boolean isRedisConnected() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (RedisConnectionFailureException e) {
            return false;
        }
    }

}
