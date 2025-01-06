package com.project.Teaming.domain.mentoring.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamUserResponse {

    private LocalDateTime acceptedTime;
    private Long userId;
    private String username;
    private MentoringRole role;
    private MentoringParticipationStatus status;
    private Boolean isLogined;
    private Boolean isDeleted;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isReviewed; // MentoringStatus가 COMPLETE일 때만 값 설정

    public TeamUserResponse(LocalDateTime acceptedTime, Long userId, String username, MentoringRole role, MentoringParticipationStatus status, Boolean isDeleted, Boolean isReviewed) {
        this.acceptedTime = acceptedTime;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.status = status;
        this.isLogined = false;
        this.isDeleted = isDeleted;
        this.isReviewed = (isReviewed != null) ? isReviewed : null; // 기본값 설정
    }
}
