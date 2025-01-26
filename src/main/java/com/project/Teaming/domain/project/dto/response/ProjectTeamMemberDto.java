package com.project.Teaming.domain.project.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.Teaming.domain.project.entity.ProjectParticipation;
import com.project.Teaming.domain.project.util.Formatter;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectTeamMemberDto {

    private Long participationId;  // pk
    private Long userId;
    private Long projectTeamId;
    private String participationStatus;
    private Boolean isDeleted;
    private Boolean isExport;
    private String decisionDate;
    private String role;
    private String recruitCategory;
    private int reportingCnt;
    @JsonProperty("isLoginUser")
    private boolean isLoginUser;  // 로그인 한 유저 본인인지 여부
    @JsonProperty("isReported")
    private boolean isReported;  // 해당 팀원을 로그인한 사용자가 신고했는지 여부
    @JsonProperty("isReviewed")
    private boolean isReviewed;  // 해당 팀원을 로그인한 사용자가 리뷰했는지 여부

    public ProjectTeamMemberDto(ProjectParticipation participation) {
        this.participationId = participation.getId();
        this.userId = participation.getUser().getId();
        this.projectTeamId = participation.getProjectTeam().getId();
        this.participationStatus = participation.getParticipationStatus().toString();
        this.isDeleted = participation.getIsDeleted();
        this.isExport = participation.getIsExport();
        this.decisionDate = participation.getDecisionDate() != null ? Formatter.getFormattedDate(participation.getDecisionDate()) : "-";
        this.role = participation.getRole().toString();
        this.recruitCategory = participation.getRecruitCategory();
        this.reportingCnt = participation.getReportingCount();
        this.isLoginUser = false;
        this.isReported = false;
        this.isReviewed = false;
    }
}
