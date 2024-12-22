package com.project.Teaming.domain.mentoring.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReportDto {
    @NotBlank
    private Long teamId;
    @NotBlank
    private Long reportedUserId;
}
