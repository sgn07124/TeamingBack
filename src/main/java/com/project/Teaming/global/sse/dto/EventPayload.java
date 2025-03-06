package com.project.Teaming.global.sse.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EventPayload {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;
    private String type;
    private String createdAt;
    private String message;
    private boolean isRead;
}
