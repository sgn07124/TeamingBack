package com.project.Teaming.domain.mentoring.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewDto {
    @NotNull
    private final Long ReviewingParticipationId;
    @NotNull
    private final Long ReviewedUserId;
    @NotNull
    private int rate;
    @NotNull
    private String content;
}
