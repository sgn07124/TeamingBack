package com.project.Teaming.domain.project.entity;

import com.project.Teaming.domain.user.entity.Report;
import com.project.Teaming.domain.user.entity.Review;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.global.auditing.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "ProjectTeam")
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTeam extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "projectId")
    private Long id;  // 프로젝트 팀 ID
    @Column(name = "projectName", nullable = false)
    private String name;  // 프로젝트 명
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
    private ProjectStatus status;
    @OneToMany(mappedBy = "projectTeam")
    private List<ProjectParticipation> teams = new ArrayList<>();
    @OneToMany(mappedBy = "projectTeam")
    private List<ProjectBoard> projectBoards = new ArrayList<>();
    @OneToMany(mappedBy = "projectTeam")
    private List<Review> reviews = new ArrayList<>();
    @OneToMany(mappedBy = "projectTeam")
    private List<Report> reports = new ArrayList<>();  // 신고 테이블과 일대다
}