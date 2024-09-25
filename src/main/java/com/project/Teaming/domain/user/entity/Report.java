package com.project.Teaming.domain.user.entity;

import com.project.Teaming.domain.project.entity.ProjectParticipation;
import com.project.Teaming.domain.project.entity.ProjectTeam;
import com.project.Teaming.global.auditing.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "Report")
@NoArgsConstructor
@AllArgsConstructor
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reportId")
    private Long id;  // 신고 ID

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ReportStatus status;  // 신고 상태

    // 외래키 : 신청 ID, 신고한 사용자 ID, 신고 당한 사용자 ID, 프로젝트 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectTeamId", referencedColumnName = "projectId")
    private ProjectTeam projectTeam;  // 프로젝트 팀 ID (FK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportedUserId", referencedColumnName = "userId")
    private User reportedUser;  // 신고 당한 사용자 ID (FK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportingUserId", referencedColumnName = "userId")
    private User reportingUser;  // 신고한 사용자 ID (FK)
}
