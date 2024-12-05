package com.project.Teaming.domain.mentoring.dto.response;

import lombok.Data;

import java.util.List;
@Data
public class LeaderResponseDto {

    private List<RsTeamUserDto> members;
    private List<RsTeamParticipationDto> participations;
}
