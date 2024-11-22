package com.project.Teaming.domain.project.entity;

import com.project.Teaming.domain.project.dto.request.CreateTeamDto;
import com.project.Teaming.domain.project.dto.request.UpdateTeamDto;
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
@Table(name = "project_team")
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTeam extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long id;  // 프로젝트 팀 ID
    @Column(name = "project_name", nullable = false)
    private String name;  // 프로젝트 명
    @Column(name = "start_date", length = 50)
    private String startDate;  // 프로젝트 시작일
    @Column(name = "end_date", length = 50)
    private String endDate;  // 프로젝트 종료일
    @Column(name = "recruit_deadline", length = 50)
    private String deadline;  // 모집 마감일
    @Column(name = "members_cnt")
    private int membersCnt;  // 모집 인원
    @Column(name = "link", length = 1000)
    private String link;  // 연락 방법
    @Column(name = "contents", columnDefinition = "TEXT")
    private String contents;  // 프로젝트 설명

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;  // 프로젝트 상태 (모집중, 진행 중, 완료)

    @OneToMany(mappedBy = "projectTeam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectParticipation> teams = new ArrayList<>();
    @OneToMany(mappedBy = "projectTeam")
    private List<ProjectBoard> projectBoards = new ArrayList<>();
    @OneToMany(mappedBy = "projectTeam")
    private List<Review> reviews = new ArrayList<>();
    @OneToMany(mappedBy = "projectTeam")
    private List<Report> reports = new ArrayList<>();  // 신고 테이블과 일대다

    @OneToMany(mappedBy = "projectTeam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamStack> stacks = new ArrayList<>();  // 기술 스택
    @OneToMany(mappedBy = "projectTeam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamRecruitCategory> recruitCategories = new ArrayList<>();  // 모집 구분

    public static ProjectTeam projectTeam(CreateTeamDto dto) {
        ProjectTeam projectTeam = new ProjectTeam();
        projectTeam.name = dto.getProjectName();
        projectTeam.startDate = dto.getStartDate();
        projectTeam.endDate = dto.getEndDate();
        projectTeam.deadline = dto.getDeadline();
        projectTeam.membersCnt = dto.getMemberCnt();
        projectTeam.link = dto.getLink();
        projectTeam.contents = dto.getContents();
        projectTeam.status = ProjectStatus.RECRUITING;
        return projectTeam;
    }

    public void updateProjectTeam(UpdateTeamDto dto) {
        this.name = dto.getProjectName();
        this.startDate = dto.getStartDate();
        this.endDate = dto.getEndDate();
        this.membersCnt = dto.getMemberCnt();
        this.link = dto.getLink();
        this.contents = dto.getContents();
    }

    public void updateStacks(List<Stack> stacks) {
        // 기존 스택 제거
        this.stacks.clear();

        // 새로 입력된 기술 스택 추가
        for (Stack stack : stacks) {
            TeamStack teamStack = new TeamStack(stack, this);
            this.stacks.add(teamStack);
        }
    }

    public void updateRecruitCategories(List<RecruitCategory> recruitCategories) {
        this.recruitCategories.clear();

        for (RecruitCategory recruitCategory : recruitCategories) {
            TeamRecruitCategory teamRecruitCategory = new TeamRecruitCategory(recruitCategory, this);
            this.recruitCategories.add(teamRecruitCategory);
        }
    }
}