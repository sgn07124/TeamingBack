package com.project.Teaming.domain.project.dto.request;

import lombok.Data;

@Data
public class ReportDto {
    private Long teamId;
    private Long reportedUserId;           // 신고 대상 사용자 ID
}
