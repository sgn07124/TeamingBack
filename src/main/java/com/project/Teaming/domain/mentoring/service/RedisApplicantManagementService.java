package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.dto.response.TeamParticipationResponse;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RedisApplicantManagementService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisApplicantManagementService(@Qualifier("participationRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Redis 키 생성
    private String generateApplicantKey(Long teamId) {
        return "mentoringTeam:" + teamId + ":applicants";
    }

    // 지원자 저장
    // 지원자 저장 및 TTL 설정
    public void saveApplicantWithTTL(Long teamId, TeamParticipationResponse response, LocalDate deadLine) {
        // 현재 시간과 deadLine 간의 차이를 계산
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadlineDateTime = deadLine.atStartOfDay();
        Duration duration = Duration.between(now, deadlineDateTime);

        long ttlSeconds = duration.getSeconds();

        if (ttlSeconds > 0) {
            String redisKey = generateApplicantKey(teamId);

            // Redis Hash에 저장 (userId -> response)
            redisTemplate.opsForHash().put(redisKey, response.getUserId(), response);

            // TTL 설정
            redisTemplate.expire(redisKey, Duration.ofSeconds(ttlSeconds));
        } else {
            log.warn("TTL not set for teamId: {}, deadLine is in the past.", teamId);
        }
    }

    // 특정 지원자 조회
    public TeamParticipationResponse getApplicant(Long teamId, String userId) {
        String redisKey = generateApplicantKey(teamId);

        // Redis Hash에서 특정 지원자 데이터 조회
        return (TeamParticipationResponse) redisTemplate.opsForHash().get(redisKey, userId);
    }

    // 특정 팀의 모든 지원자 조회
    public List<TeamParticipationResponse> getApplicants(Long teamId) {
        String redisKey = generateApplicantKey(teamId);

        // Redis Hash에서 모든 지원자 데이터 조회
        Map<Object, Object> applicantMap = redisTemplate.opsForHash().entries(redisKey);

        // Redis 데이터를 객체 리스트로 변환
        return applicantMap.values().stream()
                .map(obj -> (TeamParticipationResponse) obj)
                .collect(Collectors.toList());
    }

    // 지원자 상태 업데이트
    public void updateApplicantStatus(Long teamId, String userId, MentoringParticipationStatus newStatus) {
        String redisKey = generateApplicantKey(teamId);

        // Redis에서 지원자 데이터 가져오기
        TeamParticipationResponse response = (TeamParticipationResponse) redisTemplate.opsForHash().get(redisKey, userId);

        if (response != null) {
            // 상태 업데이트
            response.setStatus(newStatus);

            // 업데이트된 객체를 Redis에 다시 저장
            redisTemplate.opsForHash().put(redisKey, userId, response);
        } else {
            log.warn("Applicant not found for teamId: {}, userId: {}", teamId, userId);
        }
    }

    // 지원자 삭제
    public void removeApplicant(Long teamId, String userId) {
        String redisKey = generateApplicantKey(teamId);

        // Redis Hash에서 지원자 데이터 삭제
        redisTemplate.opsForHash().delete(redisKey, userId);
    }
}
