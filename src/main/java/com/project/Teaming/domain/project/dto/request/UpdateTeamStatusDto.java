package com.project.Teaming.domain.project.dto.request;

import com.project.Teaming.domain.project.entity.ProjectStatus;
import lombok.Data;

@Data
public class UpdateTeamStatusDto {

    private Long teamId;
    private ProjectStatus status;
}
