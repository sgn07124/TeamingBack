package com.project.Teaming.domain.project.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportDto {
    @NotNull
    @JsonProperty("teamId")
    private Long teamId;

    @NotNull(message = "신고 대상 사용자 ID를 입력해 주세요.")
    @JsonProperty("reportedUserId")
    private Long reportedUserId;  // 신고 대상 사용자 ID
}
