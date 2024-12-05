package com.project.Teaming.domain.mentoring.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RsTeamParticipationDto   {

    private LocalDateTime participatedTime;
    private String userId;
    private String username;
    private Integer reportingCnt;

    public RsTeamParticipationDto(LocalDateTime participatedTime, Long userId, String username, Integer reportingCnt) {
        this.participatedTime = participatedTime;
        this.userId = String.valueOf(userId);
        this.username = username;
        this.reportingCnt = reportingCnt;
    }
}
