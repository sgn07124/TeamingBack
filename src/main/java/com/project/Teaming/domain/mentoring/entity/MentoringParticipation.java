package com.project.Teaming.domain.mentoring.entity;

import com.project.Teaming.domain.mentoring.dto.request.ParticipationRequest;
import com.project.Teaming.domain.mentoring.dto.response.TeamUserResponse;
import com.project.Teaming.domain.user.entity.Report;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.global.auditing.BaseTimeEntity;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.AccessLevel;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MentoringParticipation {
    @Id
    @Tsid
    @Column(name = "mp_id")
    private Long id;  // 신청 ID

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private MentoringParticipationStatus participationStatus;  // 신청 상태

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime requestDate;  // 신청 날짜

    private LocalDateTime decisionDate;  // 신청 수락/거절 날짜

    @Enumerated(EnumType.STRING)
    private MentoringRole role;  // 역할

    @Enumerated(EnumType.STRING)  //리더, 크루
    private MentoringAuthority authority;

    // 외래키 : 신청한 사용자 ID, 멘토링 팀 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;  // 신청한 사용자 ID (주인)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentoring_team_id")
    private MentoringTeam mentoringTeam;  // 멘토링 팀 ID (주인)


    public MentoringParticipation(Long id, MentoringParticipationStatus participationStatus, LocalDateTime requestDate, LocalDateTime decisionDate, MentoringRole role, MentoringAuthority authority, int reportingCount, User user, MentoringTeam mentoringTeam) {
        this.id = id;
        this.participationStatus = participationStatus;
        this.requestDate = requestDate;
        this.decisionDate = decisionDate;
        this.role = role;
        this.authority = authority;
        this.user = user;
        this.mentoringTeam = mentoringTeam;
    }

    public MentoringParticipation(MentoringParticipationStatus participationStatus, LocalDateTime requestDate, MentoringRole role, MentoringAuthority authority) {
        this.participationStatus = participationStatus;
        this.requestDate = requestDate;
        this.role = role;
        this.authority = authority;
    }

    public static MentoringParticipation from(ParticipationRequest request) {
        return new MentoringParticipation(request.getStatus(),LocalDateTime.now(),
                request.getRole(),request.getAuthority());
    }

    public void accept() {
        this.participationStatus = MentoringParticipationStatus.ACCEPTED;
        this.authority = MentoringAuthority.CREW;
        this.decisionDate = LocalDateTime.now();
    }
    public void reject() {
        this.participationStatus = MentoringParticipationStatus.REJECTED;
    }

    public TeamUserResponse export() {
        this.participationStatus = MentoringParticipationStatus.EXPORT;
        return TeamUserResponse.toDto(this,this.getUser());
    }

    public void setLeader() {
        this.authority = MentoringAuthority.LEADER;
    }

    public void setDecisionDate(LocalDateTime decisionDate) {
        this.decisionDate = decisionDate;
    }

    public TeamUserResponse deleteParticipant() {
        return TeamUserResponse.toDto(this,this.getUser());
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