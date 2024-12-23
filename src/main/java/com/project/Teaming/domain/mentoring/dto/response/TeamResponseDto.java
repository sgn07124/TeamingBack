package com.project.Teaming.domain.mentoring.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.Teaming.domain.mentoring.entity.MentoringAuthority;
import lombok.Data;

import java.util.List;

@Data
public class TeamResponseDto {

    private MentoringAuthority Authority;
    private RsTeamDto dto;
    @JsonInclude(JsonInclude.Include.NON_NULL) // null인 경우 직렬화하지 않음
    private List<RsUserParticipationDto> userParticipations;

    public TeamResponseDto() {
        this.userParticipations = null; // 기본값
    }
}
