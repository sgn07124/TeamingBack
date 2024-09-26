package com.project.Teaming.domain.mentoring.entity;

import com.project.Teaming.domain.project.entity.ProjectStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "MentoringBoard")
@NoArgsConstructor
@AllArgsConstructor
public class MentoringBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mentoringId")
    private Long id;  // 멘토링 모집글 ID

    @Column(name = "title", nullable = false, length = 100)
    private String title;  // 모집글 제목


    @Column(name = "contents", columnDefinition = "TEXT")
    private String contents;  // 모집글 내용

    @Enumerated(EnumType.STRING)
    private MentoringRole role;  // 글을 작성한 사용자의 역할

    @Column(name = "link", length = 1000)
    private String link;  // 연락 방법

    @Enumerated(EnumType.STRING)
    private RecruitingStatus status;  // 멘토링 모집 상태

    // 멘토링 팀 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentoringTeamId")
    private MentoringTeam mentoringTeam;  // 멘토링 팀 ID (주인)
}
