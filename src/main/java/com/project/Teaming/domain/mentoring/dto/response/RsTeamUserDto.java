package com.project.Teaming.domain.mentoring.dto.response;

import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RsTeamUserDto {

    private LocalDateTime acceptedTime;
    private String userId;
    private String username;
    private MentoringRole role;
    private MentoringParticipationStatus status;
    private Boolean isDeleted;



    public RsTeamUserDto(LocalDateTime acceptedTime, Long userId, String username, MentoringRole role, MentoringParticipationStatus status, Boolean isDeleted) {
        this.acceptedTime = acceptedTime;
        this.userId = String.valueOf(userId);
        this.username = username;
        this.role = role;
        this.status = status;
        this.isDeleted = isDeleted;
    }
}
