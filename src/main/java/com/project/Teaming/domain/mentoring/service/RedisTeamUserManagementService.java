package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.dto.response.TeamUserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RedisTeamUserManagementService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisTeamUserManagementService(@Qualifier("participationRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final long TTL_SECONDS = 7 * 24 * 60 * 60; // 7일

    // Redis 키 생성
    private String generateRedisKey(Long teamId, Long userId) {
        return "mentoringTeam:" + teamId + ":user:" + userId;
    }

    private String generateTeamSetKey(Long teamId) {
        return "mentoringTeam:" + teamId + ":users";
    }

    // 사용자 전체 객체 저장 (Hash 구조로 저장)
    public void saveParticipation(Long teamId, Long userId, TeamUserResponse response) {
        String redisKey = generateRedisKey(teamId, userId);

        // Redis Hash 데이터로 저장
        Map<String, Object> hashData = new HashMap<>();
        hashData.put("response", response); // 사용자 데이터
        hashData.put("reportedCount", 0);  // 신고 횟수 초기화
        hashData.put("warningProcessed", "false"); // 초기값 설정
        redisTemplate.opsForHash().putAll(redisKey, hashData);

        // TTL 설정
        redisTemplate.expire(redisKey, Duration.ofSeconds(TTL_SECONDS));

        // 팀별 사용자 ID를 Redis Set에 추가
        String teamSetKey = generateTeamSetKey(teamId);
        redisTemplate.opsForSet().add(teamSetKey, userId.toString());
    }

    // 사용자 전체 객체 조회
    public TeamUserResponse getUser(Long teamId, Long userId) {
        String redisKey = generateRedisKey(teamId, userId);

        // Redis에서 Hash의 "response" 필드만 가져오기
        return (TeamUserResponse) redisTemplate.opsForHash().get(redisKey, "response");
    }

    // 특정 팀의 모든 사용자 조회
    public List<TeamUserResponse> getDeletedOrExportedParticipations(Long teamId) {
        String teamSetKey = generateTeamSetKey(teamId);

        // 팀별 사용자 ID Set 조회
        Set<Object> userIds = redisTemplate.opsForSet().members(teamSetKey);
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Redis에서 Hash 데이터를 한 번에 가져오기
        String redisKeyPrefix = "mentoringTeam:" + teamId + ":user:";
        List<TeamUserResponse> participations = userIds.stream()
                .map(userId -> redisTemplate.opsForHash().get(redisKeyPrefix + userId, "response"))
                .filter(Objects::nonNull)
                .map(obj -> (TeamUserResponse) obj)
                .collect(Collectors.toList());

        return participations;
    }



    public void incrementField(Long teamId, Long userId) {
        String redisKey = generateRedisKey(teamId, userId);
        redisTemplate.opsForHash().increment(redisKey, "reportedCount", 1);
    }

    public void updateWarningProcessed(Long teamId, Long userId) {
        String redisKey = generateRedisKey(teamId, userId);
        redisTemplate.opsForHash().put(redisKey, "warningProcessed", "true");
    }

    // 신고 필드 조회
    public Map<String, String> getReportFields(Long teamId, Long userId) {
        String redisKey = generateRedisKey(teamId, userId);
        Map<Object, Object> hashData = redisTemplate.opsForHash().entries(redisKey);

        if (hashData == null || hashData.isEmpty()) {
            return Collections.emptyMap();
        }

        return hashData.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> String.valueOf(entry.getKey()),
                        entry -> String.valueOf(entry.getValue())
                ));
    }
}
