package com.project.Teaming.domain.mentoring.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportDto {
    @NotNull
    private Long teamId;
    @NotNull
    private Long reportedUserId;
}
