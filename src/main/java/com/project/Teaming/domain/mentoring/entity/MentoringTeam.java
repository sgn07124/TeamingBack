package com.project.Teaming.domain.mentoring.entity;

import com.project.Teaming.global.auditing.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "mentoring_team")
@NoArgsConstructor
@AllArgsConstructor
public class MentoringTeam extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mentoring_team_id")
    private Long id;  // 멘토링 팀 ID
    @Column(name = "mentoring_name", length = 100)
    private String name;  // 멘토링 명
    @Column(name = "start_date", length = 50)
    private String startDate;  // 멘토링 시작일
    @Column(name = "end_date", length = 50)
    private String endDate;  // 멘토링 종료일
    @OneToMany(mappedBy = "mentoringTeam")
    private List<MentoringParticipation> mentoringParticipationList = new ArrayList<>();
    @OneToMany(mappedBy = "mentoringTeam")
    private List<MentoringBoard> mentoringBoardList = new ArrayList<>();

    @OneToMany(mappedBy = "mentoringTeam")
    private List<Event> eventList = new ArrayList<>();
}