package com.project.Teaming.domain.mentoring.dto.response;

import com.project.Teaming.domain.mentoring.entity.MentoringAuthority;
import lombok.Data;

import java.util.List;

@Data
public class TeamResponseDto {

    private MentoringAuthority Authority;
    private RsTeamDto dto;
}
