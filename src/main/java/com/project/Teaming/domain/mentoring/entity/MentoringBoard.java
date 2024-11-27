package com.project.Teaming.domain.mentoring.entity;

import com.project.Teaming.global.auditing.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "mentoring_board")
@NoArgsConstructor
public class MentoringBoard extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mentoring_board_id")
    private Long id;  // 멘토링 모집글 ID
    @Column(name = "title", nullable = false, length = 100)
    private String title;  // 모집글 제목
    @Column(name = "contents", columnDefinition = "TEXT")
    private String contents;  // 모집글 내용
    @Column(name = "start_date")
    private String startDate;
    @Column(name = "end_date")
    private String endDate;
    @Enumerated(EnumType.STRING)
    private MentoringRole role;  // 모집하는 역할
    @Column(name = "link", length = 1000)
    private String link;  // 연락 방법
    @Enumerated(EnumType.STRING)
    private MentoringStatus status;  // 멘토링 모집 상태
    @Enumerated(EnumType.STRING)
    private Status flag;
    // 멘토링 팀 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentoring_team_id")
    private MentoringTeam mentoringTeam;  // 멘토링 팀 ID (주인)

    @Builder
    public MentoringBoard(Long id, String title, String contents, String startDate, String endDate, MentoringRole role, String link, MentoringStatus status, Status flag, MentoringTeam mentoringTeam) {
        this.id = id;
        this.title = title;
        this.contents = contents;
        this.startDate = startDate;
        this.endDate = endDate;
        this.role = role;
        this.link = link;
        this.status = status;
        this.flag = flag;
        this.mentoringTeam = mentoringTeam;
    }



    public void setLink(String link) {
        this.link = link;
    }

    public void setFlag(Status flag) {
        this.flag = flag;
    }


    /**
     * 연관관계 편의 메서드
     */
    public void addMentoringBoard(MentoringTeam mentoringTeam) {
        this.mentoringTeam = mentoringTeam;
        mentoringTeam.getMentoringBoardList().add(this);
    }

}