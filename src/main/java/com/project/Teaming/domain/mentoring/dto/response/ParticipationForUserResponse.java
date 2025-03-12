package com.project.Teaming.domain.mentoring.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParticipationForUserResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime participatedTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
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

    public static ParticipationForUserResponse forNoAuthUser(TeamParticipationResponse participant) {
        ParticipationForUserResponse participationForUserResponse = new ParticipationForUserResponse();
        participationForUserResponse.setUserId(Long.parseLong(participant.getUserId()));
        participationForUserResponse.setParticipatedTime(participant.getParticipatedTime());
        participationForUserResponse.setUsername(participant.getUsername());
        participationForUserResponse.setStatus(participant.getStatus());
        participationForUserResponse.setIsLogined(false);
        return participationForUserResponse;
    }
}
