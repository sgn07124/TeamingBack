package com.project.Teaming.domain.mentoring.dto.request;

import com.project.Teaming.domain.mentoring.entity.MentoringAuthority;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringRole;
import lombok.Data;

@Data
public class RqParticipationDto {

    MentoringAuthority authority;
    MentoringParticipationStatus status;
    MentoringRole role;

    public RqParticipationDto(MentoringAuthority authority, MentoringParticipationStatus status, MentoringRole role) {
        this.authority = authority;
        this.status = status;
        this.role = role;
    }
}
