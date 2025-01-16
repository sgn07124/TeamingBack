package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.dto.response.TeamUserResponse;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringRole;
import com.project.Teaming.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedisParticipationManagementService {

    @Qualifier("stringRedisTemplate")
    private final StringRedisTemplate redisTemplate;

    private static final long TTL_SECONDS = 7 * 24 * 60 * 60; // 7일

    // Redis 키 생성
    private String generateRedisKey(Long teamId, Long userId) {
        return "team:" + teamId + ":user:" + userId;
    }

    // 사용자 데이터 저장
    public void saveParticipation(MentoringParticipation participation, User user) {
        Map<String, String> userData = convertToMap(participation, user);
        String redisKey = generateRedisKey(participation.getMentoringTeam().getId(), user.getId());
        redisTemplate.opsForHash().putAll(redisKey, userData);
        redisTemplate.expire(redisKey, TTL_SECONDS, TimeUnit.SECONDS); // TTL 설정
    }

    // Participation 객체 -> Redis Hash 데이터 변환
    private Map<String, String> convertToMap(MentoringParticipation participation, User user) {
        Map<String, String> userData = new HashMap<>();
        userData.put("acceptedTime", String.valueOf(participation.getDecisionDate()));
        userData.put("userId", String.valueOf(user.getId()));
        userData.put("userName", user.getName());
        userData.put("role", String.valueOf(participation.getRole()));
        userData.put("status", String.valueOf(participation.getParticipationStatus()));
        userData.put("isLogined", String.valueOf(false));
        userData.put("isReported", String.valueOf(false));
        userData.put("isReviewed", String.valueOf(false));
        userData.put("isDeleted", String.valueOf(participation.getIsDeleted()));
        userData.put("reportedCount", String.valueOf(0));
        userData.put("warningProcessed", String.valueOf(false));
        return userData;

    }

    // 특정 사용자 데이터 조회
    public Map<String, String> getUser(Long teamId, Long userId) {
        String redisKey = generateRedisKey(teamId, userId);
        Map<Object, Object> redisData = redisTemplate.opsForHash().entries(redisKey);

        // Object -> String 변환
        return redisData.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> String.valueOf(entry.getKey()),
                        entry -> String.valueOf(entry.getValue())
                ));
    }

    // 특정 팀의 모든 사용자 조회
    public List<TeamUserResponse> getDeletedOrExportedParticipations(Long teamId) {
        String pattern = "team:" + teamId + ":user:*";
        List<TeamUserResponse> participations = new ArrayList<>();

        // Redis 커넥션 사용해 키 검색
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(pattern).build());

        while (cursor.hasNext()) {
            String redisKey = new String(cursor.next(), StandardCharsets.UTF_8);

            // Redis에서 데이터 읽기
            Map<Object, Object> redisData = redisTemplate.opsForHash().entries(redisKey);

            // Redis 데이터를 TeamUserResponse로 변환
            TeamUserResponse response = mapToTeamUserResponse(redisData);
            participations.add(response);
        }

        return participations;
    }

    // Redis 데이터를 TeamUserResponse로 매핑
    private TeamUserResponse mapToTeamUserResponse(Map<Object, Object> redisData) {
        TeamUserResponse response = new TeamUserResponse();
        response.setAcceptedTime(LocalDateTime.parse((String) redisData.get("acceptedTime")));
        response.setUserId(Long.parseLong((String) redisData.get("userId")));
        response.setUsername((String) redisData.get("userName"));
        response.setRole(MentoringRole.valueOf((String) redisData.get("role")));
        response.setStatus(MentoringParticipationStatus.valueOf((String) redisData.get("status")));
        response.setIsLogined(Boolean.parseBoolean((String) redisData.get("isLogined")));
        response.setIsDeleted(Boolean.parseBoolean((String) redisData.get("isDeleted")));

        return response;
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
}