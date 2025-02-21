package com.project.Teaming.domain.project.dto.response;

import com.project.Teaming.domain.project.entity.ProjectParticipation;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectParticipationInfoDto {

    private Long participationId;  // pk
    private Long userId;
    private Long projectId;
    private String participationStatus;
    private Boolean isDeleted;
    private Boolean isExport;
    private String requestDate;
    private String decisionDate;
    private String role;
    private String recruitCategory;
    private int reportingCnt;

    public ProjectParticipationInfoDto(ProjectParticipation participation) {
        this.participationId = participation.getId();
        this.userId = participation.getUser().getId();
        this.projectId = participation.getProjectTeam().getId();
        this.participationStatus = participation.getParticipationStatus().toString();
        this.isDeleted = participation.getIsDeleted();
        this.isExport = participation.getIsExport();
        this.requestDate = getFormattedDate(participation.getRequestDate());
        this.decisionDate = participation.getDecisionDate() != null ? getFormattedDate(participation.getDecisionDate()) : "-";
        this.role = participation.getRole().toString();
        this.recruitCategory = participation.getRecruitCategory();
        this.reportingCnt = participation.getReportingCount();
    }

    public String getFormattedDate(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
}
