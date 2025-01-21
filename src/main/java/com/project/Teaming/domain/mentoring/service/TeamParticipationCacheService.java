package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.dto.response.TeamParticipationResponse;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TeamParticipationCacheService {

    private final CacheManager cacheManager;

    public TeamParticipationCacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void put(Long teamId, Map<String, TeamParticipationResponse> participation) {
        Cache cache = cacheManager.getCache("mentoringTeamParticipationCache");
        if (cache != null) {
            cache.put(teamId, participation);
        }
    }

    // 팀별 데이터 조회
    @SuppressWarnings("unchecked")
    public Map<String, TeamParticipationResponse> get(Long teamId) {
        Cache cache = cacheManager.getCache("mentoringTeamParticipationCache");
        if (cache != null) {
            return cache.get(teamId, Map.class);
        }
        return new HashMap<>(); // 데이터가 없으면 빈 Map 반환
    }

    // 팀별 데이터 삭제
    public void evict(Long teamId) {
        Cache cache = cacheManager.getCache("mentoringTeamParticipationCache");
        if (cache != null) {
            cache.evict(teamId);
        }
    }
    public void clear() {
        Cache cache = cacheManager.getCache("mentoringTeamParticipationCache");
        if (cache != null) {
            cache.clear(); // 전체 캐시 삭제
        }
    }
}
