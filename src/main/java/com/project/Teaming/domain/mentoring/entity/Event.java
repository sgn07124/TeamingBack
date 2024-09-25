package com.project.Teaming.domain.mentoring.entity;

import com.project.Teaming.global.auditing.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "MentoringEvent")
@NoArgsConstructor
@AllArgsConstructor
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "eventId")
    private Long id;  // 일정 ID

    @Column(name = "title", nullable = false, length = 100)
    private String title;  // 제목

    @Column(name = "content", columnDefinition = "TEXT")
    private String contents;  // 내용

    @Column(name = "startDate", length = 50)
    private String startDate;  // 시작 날짜

    @Column(name = "endDate", length = 50)
    private String endDate;  // 종료 날짜

    // 외래키 : 멘토링 팀 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentoringTeamId")
    private MentoringTeam mentoringTeam;  // 멘토링 팀 ID (주인)
}
