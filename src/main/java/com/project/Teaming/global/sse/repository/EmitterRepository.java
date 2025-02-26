package com.project.Teaming.global.sse.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
@RequiredArgsConstructor
public class EmitterRepository {

    private static final String SSE_EMITTER_KEY = "SSE_EMITTERS";
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60 ;
    private final StringRedisTemplate redisTemplate;
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter findById(Long userId) {

        if (emitters.containsKey(userId)) {
            return emitters.get(userId);
        }

        if (Boolean.TRUE.equals(redisTemplate.hasKey(SSE_EMITTER_KEY + ":" + userId))) {

            Long ttl = redisTemplate.getExpire(SSE_EMITTER_KEY + ":" + userId, TimeUnit.MILLISECONDS);

            if (ttl == null || ttl <= 0) {
                redisTemplate.delete(SSE_EMITTER_KEY + ":" + userId);
                throw new BusinessException(ErrorCode.NOT_CONNECTED);
            }

            SseEmitter emitter = new SseEmitter(ttl);

            emitters.put(userId, emitter);
            return emitters.get(userId);
        } else {
            throw new BusinessException(ErrorCode.NOT_CONNECTED);
        }
    }

    public SseEmitter save(Long userId, SseEmitter sseEmitter) {
        emitters.put(userId, sseEmitter);
        // Redis에도 유저 SSE 정보 저장
        redisTemplate.opsForValue().set(
                SSE_EMITTER_KEY + ":" + userId,
                "active",
                DEFAULT_TIMEOUT,
                TimeUnit.MILLISECONDS
        );
        return emitters.get(userId);
    }

    public void deleteById(Long userId) {
        emitters.remove(userId);
        redisTemplate.delete(SSE_EMITTER_KEY + ":" + userId);
    }
}
