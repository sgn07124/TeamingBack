package com.project.Teaming.domain.mentoring.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.Teaming.domain.mentoring.entity.MentoringAuthority;
import lombok.Data;

import java.util.List;

@Data
public class TeamAuthorityResponse {

    private MentoringAuthority Authority;
    private TeamResponse dto;
    @JsonInclude(JsonInclude.Include.NON_NULL) // null인 경우 직렬화하지 않음
    private List<ParticipationForUserResponse> userParticipations;

    public TeamAuthorityResponse() {
        this.userParticipations = null; // 기본값
    }
}
