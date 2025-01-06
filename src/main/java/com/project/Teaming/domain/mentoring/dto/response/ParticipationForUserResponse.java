package com.project.Teaming.domain.mentoring.dto.response;

import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ParticipationForUserResponse {

    private LocalDateTime participatedTime;
    private Long userId;
    private String username;
    private MentoringParticipationStatus status;
    private Boolean isLogined;

    public ParticipationForUserResponse(LocalDateTime participatedTime, Long userId, String username, MentoringParticipationStatus status) {
        this.participatedTime = participatedTime;
        this.userId = userId;
        this.username = username;
        this.status = status;
        this.isLogined = false;
    }
}
