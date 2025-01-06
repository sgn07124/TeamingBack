package com.project.Teaming.domain.mentoring.entity;

import com.project.Teaming.domain.mentoring.dto.response.TeamResponse;
import com.project.Teaming.domain.mentoring.dto.request.TeamRequest;
import com.project.Teaming.global.auditing.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "mentoring_team")
public class MentoringTeam extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mentoring_team_id")
    private Long id;  // 멘토링 팀 ID
    @Column(name = "mentoring_name", length = 100)
    private String name;  // 멘토링 명
    @Column(name = "start_date", length = 50)
    private LocalDate startDate;  // 멘토링 시작일
    @Column(name = "end_date", length = 50)
    private LocalDate endDate;  // 멘토링 종료일
    @Column(name = "mentoring_cnt")
    private Integer mentoringCnt;
    @Column(name = "content")
    private String content;
    @Column(name = "status")
    private MentoringStatus status;
    @Column(name = "link")
    private String link;
    @Column(name = "flag")
    private Status flag;
    @OneToMany(mappedBy = "mentoringTeam",cascade = CascadeType.PERSIST)
    private List<MentoringParticipation> mentoringParticipationList;
    @OneToMany(mappedBy = "mentoringTeam",orphanRemoval = true)
    private List<MentoringBoard> mentoringBoardList;
    @OneToMany(mappedBy = "mentoringTeam")
    private List<Event> eventList;
    @OneToMany(mappedBy = "mentoringTeam")
    private List<TeamCategory> categories;

    public MentoringTeam() {
        this.mentoringParticipationList = new ArrayList<>();
        this.mentoringBoardList = new ArrayList<>();
        this.eventList = new ArrayList<>();
        this.categories = new ArrayList<>();
    }

    @Builder
    public MentoringTeam(Long id, String name, LocalDate startDate, LocalDate endDate, Integer mentoringCnt, String content, MentoringStatus status, String link, Status flag, List<MentoringParticipation> mentoringParticipationList, List<MentoringBoard> mentoringBoardList, List<Event> eventList, List<TeamCategory> categories) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.mentoringCnt = mentoringCnt;
        this.content = content;
        this.status = status;
        this.link = link;
        this.flag = flag;
        this.mentoringParticipationList = mentoringParticipationList != null ? mentoringParticipationList : new ArrayList<>();
        this.mentoringBoardList = mentoringBoardList != null ? mentoringBoardList : new ArrayList<>();
        this.eventList = eventList != null ? eventList : new ArrayList<>();
        this.categories = categories != null ? categories : new ArrayList<>();
    }

    public void setFlag(Status flag) {
        this.flag = flag;
    }

    public TeamResponse toDto() {
        TeamResponse dto = TeamResponse.builder()
                .id(this.getId())
                .name(this.getName())
                .startDate(this.getStartDate())
                .mentoringCnt(this.getMentoringCnt())
                .endDate(this.getEndDate())
                .content(this.getContent())
                .status(this.getStatus())
                .link(this.getLink())
                .build();
        return dto;
    }

    public void mentoringTeamUpdate(TeamRequest dto) {
        this.name = dto.getName();
        this.startDate = dto.getStartDate();
        this.endDate = dto.getEndDate();
        this.mentoringCnt = dto.getMentoringCnt();
        this.content = dto.getContent();
        this.link = dto.getLink();
    }
}