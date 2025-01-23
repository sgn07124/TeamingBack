package com.project.Teaming.domain.mentoring.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MentoringReviewRequest {
    @NotNull
    private Long teamId;
    @NotNull
    private Long ReviewedUserId;
    @NotNull
    private int rate;
    @NotNull
    private String content;
}
