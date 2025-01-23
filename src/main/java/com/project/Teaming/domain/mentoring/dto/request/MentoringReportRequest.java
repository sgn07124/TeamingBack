package com.project.Teaming.domain.mentoring.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MentoringReportRequest {
    @NotNull
    private Long teamId;
    @NotNull
    private Long reportedUserId;
}
