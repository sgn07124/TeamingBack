package com.project.Teaming.domain.mentoring.dto.request;

import com.project.Teaming.domain.mentoring.entity.MentoringRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeamRequestDto {

    @NotBlank
    private MentoringRole role;
    private RqTeamDto mentoringTeamDto;
}
