package com.project.Teaming.domain.mentoring.dto.request;

import com.project.Teaming.domain.mentoring.entity.MentoringAuthority;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringRole;
import lombok.Data;

@Data
public class ParticipationRequest {

    MentoringAuthority authority;
    MentoringParticipationStatus status;
    MentoringRole role;

    public ParticipationRequest(MentoringAuthority authority, MentoringParticipationStatus status, MentoringRole role) {
        this.authority = authority;
        this.status = status;
        this.role = role;
    }
}
