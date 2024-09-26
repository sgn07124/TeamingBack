package com.project.Teaming.domain.mentoring.entity;

import com.project.Teaming.domain.project.entity.ParticipationStatus;
import com.project.Teaming.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "MentoringParticipation")
@NoArgsConstructor
@AllArgsConstructor
public class MentoringParticipation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mpId")
    private Long id;  // 신청 ID
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ParticipationStatus participationStatus;  // 신청 상태
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime requestDate;  // 신청 날짜
    private LocalDateTime decisionDate;  // 신청 수락/거절 날짜
    @Enumerated(EnumType.STRING)
    private MentoringRole role;  // 역할
    // 외래키 : 신청한 사용자 ID, 멘토링 팀 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;  // 신청한 사용자 ID (주인)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentoringTeamId")
    private MentoringTeam mentoringTeam;  // 멘토링 팀 ID (주인)
}