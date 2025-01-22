package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.dto.response.TeamUserResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RedisParticipationManagementService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisParticipationManagementService(@Qualifier("participationRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final long TTL_SECONDS = 7 * 24 * 60 * 60; // 7일

    // Redis 키 생성
    private String generateRedisKey(Long teamId, Long userId) {
        return "mentoringTeam:" + teamId + ":user:" + userId;
    }

    // 사용자 전체 객체 저장
    public void saveParticipation(Long teamId, Long userId, TeamUserResponse response) {
        String redisKey = generateRedisKey(teamId, userId);
        redisTemplate.opsForValue().set(redisKey, response, Duration.ofSeconds(TTL_SECONDS));
    }

    // 사용자 전체 객체 조회 (Hash 데이터 병합)
    public TeamUserResponse getUser(Long teamId, Long userId) {
        String redisKey = generateRedisKey(teamId, userId);
        return (TeamUserResponse) redisTemplate.opsForValue().get(redisKey);
    }

    // 특정 팀의 모든 사용자 조회
    public List<TeamUserResponse> getDeletedOrExportedParticipations(Long teamId) {
        String pattern = "mentoringTeam:" + teamId + ":user:*";
        List<TeamUserResponse> participations = new ArrayList<>();

        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(pattern).build());

        while (cursor.hasNext()) {
            String redisKey = new String(cursor.next(), StandardCharsets.UTF_8);
            TeamUserResponse response = (TeamUserResponse) redisTemplate.opsForValue().get(redisKey);
            if (response != null) {
                participations.add(response);
            }
        }
        return participations;
    }



    // 특정 필드 업데이트 (예: 신고 횟수 증가)
    public void incrementField(Long teamId, Long userId) {
        String redisKey = generateRedisKey(teamId, userId);
        redisTemplate.opsForHash().increment(redisKey, "reportedCount", 1);
    }

    public void updateWarningProcessed(Long teamId, Long userId) {
        String redisKey = generateRedisKey(teamId, userId);
        redisTemplate.opsForHash().put(redisKey, "warningProcessed", "true");
    }

    // 신고 필드 조회 (Hash 기반)
    public Map<String, String> getReportFields(Long teamId, Long userId) {
        String redisKey = generateRedisKey(teamId, userId);
        Map<Object, Object> hashData = redisTemplate.opsForHash().entries(redisKey);
        return hashData.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> String.valueOf(entry.getKey()),
                        entry -> String.valueOf(entry.getValue())
                ));
    }
}