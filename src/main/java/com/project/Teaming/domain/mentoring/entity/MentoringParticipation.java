package com.project.Teaming.domain.mentoring.entity;

import com.project.Teaming.domain.user.entity.Report;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.global.auditing.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "mentoring_participation")
@NoArgsConstructor
public class MentoringParticipation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mp_id")
    private Long id;  // 신청 ID
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private MentoringParticipationStatus
            participationStatus;  // 신청 상태
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime requestDate;  // 신청 날짜
    private LocalDateTime decisionDate;  // 신청 수락/거절 날짜
    @Enumerated(EnumType.STRING)
    private MentoringRole role;  // 역할
    @Enumerated(EnumType.STRING)  //리더, 크루
    private MentoringAuthority authority;
    @Column(name = "reporting_cnt")
    private int reportingCnt;
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
    @Column(name = "warning_processed", nullable = false)
    private Boolean warningProcessed = false; // 경고 처리 여부
    // 외래키 : 신청한 사용자 ID, 멘토링 팀 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;  // 신청한 사용자 ID (주인)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentoring_team_id")
    private MentoringTeam mentoringTeam;  // 멘토링 팀 ID (주인)

    @Builder
    public MentoringParticipation(Long id, MentoringParticipationStatus participationStatus, LocalDateTime requestDate, LocalDateTime decisionDate, MentoringRole role, MentoringAuthority authority, int reportingCnt, User user, MentoringTeam mentoringTeam) {
        this.id = id;
        this.participationStatus = participationStatus;
        this.requestDate = requestDate;
        this.decisionDate = decisionDate;
        this.role = role;
        this.authority = authority;
        this.reportingCnt = reportingCnt;
        this.isDeleted = false;
        this.warningProcessed = false;
        this.user = user;
        this.mentoringTeam = mentoringTeam;
    }


    public void setParticipationStatus(MentoringParticipationStatus participationStatus) {
        this.participationStatus = participationStatus;
    }

    public void setDecisionDate(LocalDateTime decisionDate) {
        this.decisionDate = decisionDate;
    }

    public void setAuthority(MentoringAuthority authority) {
        this.authority = authority;
    }
    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public void setReportingCnt(int reportingCnt) {
        this.reportingCnt = reportingCnt;
    }
    public void setWarningProcessed(Boolean warningProcessed) {
        this.warningProcessed = warningProcessed;
    }

    /**
     *연관관계 편의 메서드
     */

    public void setUser(User user) {
        this.user = user;
        user.getMentoringParticipations().add(this);
    }

    public void addMentoringTeam(MentoringTeam mentoringTeam) {
        this.mentoringTeam = mentoringTeam;
        mentoringTeam.getMentoringParticipationList().add(this);
    }
    public void removeUser(User user) {
        if (this.user != null) {
            this.user.getMentoringParticipations().remove(this);
            this.user = null;
        }
    }
    public void removeMentoringTeam(MentoringTeam mentoringTeam) {
        if (this.mentoringTeam != null) {
            this.mentoringTeam.getMentoringParticipationList().remove(this);
            this.mentoringTeam = null;
        }
    }
}