package com.project.Teaming.domain.project.entity;

import com.project.Teaming.domain.project.dto.request.CreatePostDto;
import com.project.Teaming.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "project_board")
@NoArgsConstructor
@AllArgsConstructor
public class ProjectBoard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pj_post_id")
    private Long id;  // 프로젝트 모집글 ID

    @Column(name = "pj_post_title", nullable = false, length = 100)
    private String title;  // 제목

    @Column(name = "recruit_deadline", length = 50)
    private String deadline;  // 모집 마감일

    @Column(name = "members_cnt")
    private int membersCnt;  // 모집 인원

    @Column(name = "link", length = 1000)
    private String link;  // 연락 방법

    @Column(name = "contents", columnDefinition = "TEXT")
    private String contents;  // 프로젝트 설명

    @Enumerated(EnumType.STRING)
    private PostStatus status;  // 프로젝트 게시글 모집 상태

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ProjectTeam projectTeam;  // 주인

    public static ProjectBoard projectBoard(CreatePostDto dto, ProjectTeam projectTeam) {
        ProjectBoard post = new ProjectBoard();
        post.title = dto.getTitle();
        post.deadline = dto.getDeadline();
        post.membersCnt = dto.getMemberCnt();
        post.link = dto.getLink();
        post.contents = dto.getContents();
        post.status = PostStatus.RECRUITING;
        post.projectTeam = projectTeam;
        return post;
    }

    public void updateProjectBoard(CreatePostDto dto, ProjectTeam projectTeam) {
        this.title = dto.getTitle();
        this.deadline = dto.getDeadline();
        this.membersCnt = dto.getMemberCnt();
        this.link = dto.getLink();
        this.contents = dto.getContents();
        this.status = PostStatus.RECRUITING;
        this.projectTeam = projectTeam;
    }
}