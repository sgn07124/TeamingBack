package com.project.Teaming.domain.project.dto.request;

import com.project.Teaming.domain.project.entity.ProjectStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTeamStatusDto {

    @NotNull
    private Long teamId;
    @NotNull(message = "팀 상태를 입력해 주세요. ex) RECRUITING, WORKING, COMPLETE")
    private ProjectStatus status;
}
