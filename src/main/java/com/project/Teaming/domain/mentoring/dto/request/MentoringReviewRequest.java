package com.project.Teaming.domain.mentoring.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MentoringReviewRequest {
    @NotNull
    private final Long teamId;
    @NotNull
    private final Long ReviewedUserId;
    @NotNull
    private int rate;
    @NotNull
    private String content;
}
