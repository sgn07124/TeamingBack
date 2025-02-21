package com.project.Teaming.domain.mentoring.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import com.project.Teaming.domain.user.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class TeamParticipationResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
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

    public static TeamParticipationResponse toParticipationDto(MentoringParticipation mentoringParticipation) {
        TeamParticipationResponse participation = new TeamParticipationResponse();
        User user = mentoringParticipation.getUser();
        participation.setParticipatedTime(mentoringParticipation.getRequestDate());
        participation.setUserId(String.valueOf(user.getId()));
        participation.setUsername(user.getName());
        participation.setReportingCnt(user.getWarningCount());
        participation.setStatus(mentoringParticipation.getParticipationStatus());
        return participation;
    }
}
