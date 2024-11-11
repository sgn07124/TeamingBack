package com.project.Teaming.domain.mentoring.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class TeamResponseDto {

    private RsTeamDto dto;
    private List<RsParticipationDto> userDto;

}
