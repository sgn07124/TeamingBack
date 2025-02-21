package com.project.Teaming.domain.user.entity;

import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.project.entity.ProjectParticipation;
import com.project.Teaming.domain.project.entity.ProjectTeam;
import com.project.Teaming.global.auditing.BaseTimeEntity;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "report")
@NoArgsConstructor
@AllArgsConstructor
public class Report extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;  // 신고 ID

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ReportStatus status;  // 신고 상태

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_participation_id", referencedColumnName = "par_id",nullable = true)
    private ProjectParticipation projectParticipation;  // 프로젝트 유저 ID (FK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentoring_participation_id", referencedColumnName = "mp_id",nullable = true)
    private MentoringParticipation mentoringParticipation;  // 멘토링 유저 ID (FK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id", referencedColumnName = "user_id", nullable = false)
    private User reportedUser;  // 신고 당한 사용자 ID (FK)

    @Column(name = "warning_processed")
    private boolean warningProcessed; // warningCnt 증가 처리 여부

    @PrePersist
    @PreUpdate
    private void validate() {
        if ((mentoringParticipation == null && projectParticipation == null) ||
                (mentoringParticipation != null && projectParticipation != null)) {
            throw new BusinessException(ErrorCode.CHOOSE_ONE_DOMAIN);
        }
    }

    public static Report projectReport(ProjectParticipation reporterParticipation, User reportedUser) {
        Report report = new Report();
        report.projectParticipation = reporterParticipation;
        report.reportedUser = reportedUser;
        report.status = ReportStatus.REPORTED;
        report.warningProcessed = false;
        return report;
    }
    public static Report mentoringReport(MentoringParticipation reportingParticipation, User reportedUser) {
        Report report = new Report();
        report.mentoringParticipation = reportingParticipation;
        report.reportedUser = reportedUser;
        report.status = ReportStatus.REPORTED;
        return report;
    }

}