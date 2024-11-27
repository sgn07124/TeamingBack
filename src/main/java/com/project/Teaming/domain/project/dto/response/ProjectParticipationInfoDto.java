package com.project.Teaming.domain.project.dto.response;

import com.project.Teaming.domain.project.entity.ProjectParticipation;
import lombok.Data;

@Data
public class ProjectParticipationInfoDto {

    private Long participationId;  // pk
    private Long userId;
    private Long projectId;
    private String participationStatus;
    private Boolean isDeleted;
    private String requestDate;
    private String decisionDate;
    private String role;
    private int reportingCnt;

    public ProjectParticipationInfoDto(ProjectParticipation participation) {
        this.participationId = participation.getId();
        this.userId = participation.getUser().getId();
        this.projectId = participation.getProjectTeam().getId();
        this.participationStatus = participation.getParticipationStatus().toString();
        this.isDeleted = participation.getIsDeleted();
        this.requestDate = participation.getRequestDate().toString();
        this.decisionDate = participation.getDecisionDate().toString();
        this.role = participation.getRole().toString();
        this.reportingCnt = participation.getReportingCnt();
    }
}
