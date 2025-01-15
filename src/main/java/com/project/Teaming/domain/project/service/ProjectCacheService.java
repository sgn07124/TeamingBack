package com.project.Teaming.domain.project.service;

import com.project.Teaming.domain.project.dto.response.ProjectPostListDto;
import com.project.Teaming.global.result.pagenateResponse.PaginatedCursorResponse;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectCacheService {

    private final RedisTemplate<String, Object> postRedisTemplate;
    private static final String POSTS_CACHE_KEY = "project_posts:";

    // 캐시 조회 메서드
    public PaginatedCursorResponse<ProjectPostListDto> getCachePosts(Long cursor, int pageSize) {
        String cacheKey = POSTS_CACHE_KEY + cursor + ":" + pageSize;
        return (PaginatedCursorResponse<ProjectPostListDto>) postRedisTemplate.opsForValue().get(cacheKey);
    }

    // 캐시 갱신 메서드
    public void cachePosts(Long cursor, int pageSize, PaginatedCursorResponse<ProjectPostListDto> posts) {
        String cacheKey = POSTS_CACHE_KEY + cursor + ":" + pageSize;
        // jitter 개념을 적용. 캐시 만료 시간을 무작위로 조금 지연시켜서 DB 부하를 분산시키는 용도로 사용
        int jitter = new Random().nextInt(10);  // 0~9초 랜덤
        postRedisTemplate.opsForValue().set(cacheKey, posts, 1 + jitter, TimeUnit.MINUTES);
    }

    // 캐시 삭제 메서드
    public void evictCache(Long cursor, int pageSize) {
        String cacheKey = POSTS_CACHE_KEY + cursor + ":" + pageSize;
        postRedisTemplate.delete(cacheKey);
    }
}
