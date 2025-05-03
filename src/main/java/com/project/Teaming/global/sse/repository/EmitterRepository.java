package com.project.Teaming.global.sse.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
@RequiredArgsConstructor
public class EmitterRepository {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter findById(Long userId) {
        return emitters.get(userId);
    }

    public SseEmitter save(Long userId, SseEmitter sseEmitter) {
        emitters.put(userId, sseEmitter);
        return emitters.get(userId);
    }

    public void deleteById(Long userId) {
        emitters.remove(userId);
    }
}
