package com.project.Teaming.domain.mentoring.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MentoringReportRequest {

    @NotNull
    @JsonDeserialize(using = NumberDeserializers.LongDeserializer.class) // String을 Long으로 변환
    private Long teamId;

    @NotNull
    @JsonDeserialize(using = NumberDeserializers.LongDeserializer.class) // String을 Long으로 변환
    private Long reportedUserId;
}
