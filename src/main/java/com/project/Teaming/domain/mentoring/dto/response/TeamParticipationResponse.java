package com.project.Teaming.domain.mentoring.dto.response;

import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamParticipationResponse {

    private LocalDateTime participatedTime;
    private String userId;
    private String username;
    private Integer reportingCnt;
    private MentoringParticipationStatus status;


    public TeamParticipationResponse(LocalDateTime participatedTime, Long userId, String username, Integer reportingCnt, MentoringParticipationStatus status) {
        this.participatedTime = participatedTime;
        this.userId = String.valueOf(userId);
        this.username = username;
        this.reportingCnt = reportingCnt;
        this.status = status;
    }
}
