package com.project.Teaming.domain.project.entity;

import com.project.Teaming.domain.user.entity.Report;
import com.project.Teaming.domain.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "project_participation")
@NoArgsConstructor
@AllArgsConstructor
public class ProjectParticipation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "par_id")
    private Long id;  // 신청 ID
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ParticipationStatus participationStatus;  // 신청 상태
    @Column(name = "is_deleted")
    private Boolean isDeleted;  // 탈퇴 여부 (초기값 : false)
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime requestDate;  // 신청일
    private LocalDateTime decisionDate;  // 신청 수락/거절 날짜
    @Enumerated(EnumType.STRING)
    private ProjectRole role;  // 역할
    @Column(name = "reporting_cnt", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int reportingCnt;  // 신고 누적
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;  // 사용자 ID (주인)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ProjectTeam projectTeam;  // 프로젝트 팀 ID (주인)

    public void createProjectParticipation(User user, ProjectTeam team) {
        this.participationStatus = ParticipationStatus.ACCEPTED;
        this.isDeleted = false;
        this.requestDate = LocalDateTime.now();
        this.decisionDate = LocalDateTime.now();
        this.role = ProjectRole.OWNER;
        this.user = user;
        this.projectTeam = team;
    }
}