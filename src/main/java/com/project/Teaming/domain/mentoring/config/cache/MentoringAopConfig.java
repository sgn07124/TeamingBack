package com.project.Teaming.domain.mentoring.config.cache;


import com.project.Teaming.domain.mentoring.dto.response.TeamParticipationResponse;
import com.project.Teaming.domain.mentoring.entity.MentoringAuthority;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.mentoring.service.TeamParticipationCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Aspect
@RequiredArgsConstructor
public class MentoringAopConfig {

    private final TeamParticipationCacheService cacheService;

    // 멘토링 관련 서비스 Pointcut 정의
    // 팀 ID가 첫 번째 매개변수로 전달되는 메서드에 적용
    @Pointcut("execution(* com.project.Teaming.domain.mentoring.service.MentoringParticipationService.saveMentoringParticipation(..)) || " +
            "execution(* com.project.Teaming.domain.mentoring.service.MentoringParticipationService.acceptMentoringParticipation(..)) || " +
            "execution(* com.project.Teaming.domain.mentoring.service.MentoringParticipationService.rejectMentoringParticipation(..))")
    public void methodsForCacheUpdate() {}

    //삭제할 때 이용할 서비스 Pointcut 정의
    @Pointcut("execution(* com.project.Teaming.domain.mentoring.service..cancelMentoringParticipation(..)) && args(teamId, ..)")
    public void cancelMentoringParticipationPointcut(Long teamId) {}


    // AfterReturning: 메서드 실행 후 캐싱 처리
    @AfterReturning(pointcut = "methodsForCacheUpdate()", returning = "result")
    public void updateCache(JoinPoint joinPoint, Object result) {
        try {
            Long teamId = (Long) joinPoint.getArgs()[0]; // 첫 번째 매개변수 추출
            log.info("Triggered updateCache: teamId={}, result={}", teamId, result);

            if (result instanceof TeamParticipationResponse) {
                handleDtoCaching(teamId, (TeamParticipationResponse) result);
            } else if (result instanceof MentoringParticipation) {
                handleEntityCaching(teamId, (MentoringParticipation) result);
            } else {
                log.warn("Unsupported return type for caching: {}", result != null ? result.getClass().getName() : "null");
            }
        } catch (Exception e) {
            log.error("Failed to update cache", e);
        }
    }

    private void handleDtoCaching(Long teamId, TeamParticipationResponse response) {
        // 캐시에서 기존 데이터 가져오기
        Map<String, TeamParticipationResponse> cachedDtos = cacheService.get(teamId);
        if (cachedDtos == null) {
            cachedDtos = new HashMap<>();
        }

        // 캐시에 DTO 추가 또는 업데이트
        cachedDtos.put(String.valueOf(response.getUserId()), response);

        // 캐시 갱신
        cacheService.put(teamId, cachedDtos);

        log.info("Cached DTO for teamId: {} " ,teamId);
    }
    private void handleEntityCaching(Long teamId, MentoringParticipation participation) {
        // LEADER가 아닌 경우에만 캐싱
        if (participation.getAuthority() != MentoringAuthority.LEADER) {
            TeamParticipationResponse dto = TeamParticipationResponse.toParticipationDto(participation);

            // 캐시에서 기존 데이터 가져오기
            Map<String, TeamParticipationResponse> cachedDtos = cacheService.get(teamId);
            if (cachedDtos == null) {
                cachedDtos = new HashMap<>();
            }

            // 캐시에 DTO 추가 또는 업데이트
            cachedDtos.put(String.valueOf(dto.getUserId()), dto);

            // 캐시 갱신
            cacheService.put(teamId, cachedDtos);

            log.info("Cached DTO for participation in teamId: {}", teamId);
        }
    }
    @AfterReturning(pointcut = "cancelMentoringParticipationPointcut(teamId)", returning = "result")
    public void removeFromCacheAfterCancellation(Long teamId, Object result) {
        if (result instanceof TeamParticipationResponse) {
            TeamParticipationResponse response = (TeamParticipationResponse) result;

            try {
                // 캐시에서 데이터 삭제
                Map<String, TeamParticipationResponse> cachedDtos = cacheService.get(teamId);
                log.debug("Cached data for teamId {} before removal: {}", teamId, cachedDtos);
                if (cachedDtos != null) {
                    cachedDtos.remove(String.valueOf(response.getUserId()));
                    cacheService.put(teamId, cachedDtos); // 캐시 갱신
                    log.info("Removed userId: {} from cache for teamId: {}", response.getUserId(), teamId);
                }
            }catch (Exception e) {
                log.error("Failed to remove cache for teamId: {}, userId: {}", teamId, response.getUserId(), e);
            }
        }
    }
}
