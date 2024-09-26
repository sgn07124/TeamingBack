package com.project.Teaming.domain.project.entity;

import com.project.Teaming.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "ProjectBoard")
@NoArgsConstructor
@AllArgsConstructor
public class ProjectBoard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pjPostId")
    private Long id;  // 프로젝트 모집글 ID
    @Column(name = "pjPostTitle", nullable = false, length = 100)
    private String title;  // 프로젝트 명
    @Column(name = "startDate", length = 50)
    private String startDate;  // 프로젝트 시작일
    @Column(name = "endDate", length = 50)
    private String endDate;  // 프로젝트 종료일
    @Column(name = "membersCnt")
    private int membersCnt;  // 모집 인원
    @Column(name = "link", length = 1000)
    private String link;  // 연락 방법
    @Column(name = "contents", columnDefinition = "TEXT")
    private String contents;  // 프로젝트 설명
    @Enumerated(EnumType.STRING)
    private ProjectStatus status;  // 프로젝트 모집 상태
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectId")
    private ProjectTeam projectTeam;  // 주인
}