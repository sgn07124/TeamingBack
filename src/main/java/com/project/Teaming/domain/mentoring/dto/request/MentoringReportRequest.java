package com.project.Teaming.domain.mentoring.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MentoringReportRequest {

    @NotNull
    @JsonProperty("teamId")
    private Long teamId;

    @NotNull
    @JsonProperty("reportedUserId")
    private Long reportedUserId;
}
