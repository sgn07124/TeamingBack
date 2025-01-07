package com.project.Teaming.domain.project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportDto {
    @NotNull
    private Long teamId;
    @NotNull(message = "신고 대상 사용자 ID를 입력해 주세요.")
    private Long reportedUserId;  // 신고 대상 사용자 ID
}
