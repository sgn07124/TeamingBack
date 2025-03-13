package com.project.Teaming.global.sse.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class EventWithTeamPayload {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;
    private String type;
    private String category;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long teamId;
    private String createdAt;
    private String message;
    private boolean isRead;

    @Builder
    public EventWithTeamPayload(Long userId, String type, String category, Long teamId, String createdAt, String message, boolean isRead) {
        this.userId = userId;
        this.type = type;
        this.category = category;
        this.teamId = teamId;
        this.createdAt = createdAt;
        this.message = message;
        this.isRead = isRead;
    }
}
