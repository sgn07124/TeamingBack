package com.project.Teaming.global.sse.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class EventPayload {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;
    private String type;
    private String category;
    private String createdAt;
    private String message;
    private boolean isRead;

    @Builder
    public EventPayload(Long userId, String type, String category, String createdAt, String message, boolean isRead) {
        this.userId = userId;
        this.type = type;
        this.category = category;
        this.createdAt = createdAt;
        this.message = message;
        this.isRead = isRead;
    }
}
