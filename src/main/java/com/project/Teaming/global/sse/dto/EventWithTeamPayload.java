package com.project.Teaming.global.sse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EventWithTeamPayload {

    private Long userId;
    private String type;
    private Long teamId;
    private String createdAt;
    private String message;
    private boolean isRead;
}
