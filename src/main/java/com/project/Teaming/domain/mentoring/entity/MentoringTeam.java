package com.project.Teaming.domain.mentoring.entity;

import com.project.Teaming.domain.mentoring.dto.response.TeamResponse;
import com.project.Teaming.domain.mentoring.dto.request.TeamRequest;
import com.project.Teaming.global.auditing.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    @Enumerated(EnumType.STRING)
    private MentoringStatus status;
    @Column(name = "link")
    private String link;
    @Column(name = "flag")
    @Enumerated(EnumType.STRING)
    private Status flag;
    @OneToMany(mappedBy = "mentoringTeam",cascade = CascadeType.PERSIST)
    private List<MentoringParticipation> mentoringParticipationList = new ArrayList<>();
    @OneToMany(mappedBy = "mentoringTeam",orphanRemoval = true)
    private List<MentoringBoard> mentoringBoardList = new ArrayList<>();
    @OneToMany(mappedBy = "mentoringTeam")
    private List<Event> eventList = new ArrayList<>();
    @OneToMany(mappedBy = "mentoringTeam")
    private List<TeamCategory> categories = new ArrayList<>();

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
        this.mentoringParticipationList = mentoringParticipationList;
        this.mentoringBoardList = mentoringBoardList;
        this.eventList = eventList;
        this.categories = categories;
    }

    public MentoringTeam(String name, LocalDate startDate, LocalDate endDate, Integer mentoringCnt, String content, MentoringStatus status, String link, Status flag) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.mentoringCnt = mentoringCnt;
        this.content = content;
        this.status = status;
        this.link = link;
        this.flag = flag;
    }
    public static MentoringTeam from(TeamRequest dto) {
        return new MentoringTeam(
                dto.getName(),dto.getStartDate(),dto.getEndDate(),dto.getMentoringCnt(),dto.getContent(),
                MentoringStatus.RECRUITING, dto.getLink(), Status.FALSE);
    }

    public void flag(Status flag) {
        this.flag = flag;
    }

    public TeamResponse toDto() {
        return TeamResponse.from(this);
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