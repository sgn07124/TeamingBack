package com.project.Teaming.domain.mentoring.entity;

import com.project.Teaming.domain.mentoring.dto.request.RqBoardDto;
import com.project.Teaming.domain.mentoring.dto.response.RsSpecBoardDto;
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
    @Enumerated(EnumType.STRING)
    private MentoringRole role;  // 모집하는 역할, 팀과 컬럼의 역할이 다름
    @Column
    private Integer mentoringCnt;  //수정할 수 있도록 팀과 칼럼중복
    @Column(name = "link", length = 1000)
    private String link;  // 연락 방법
    // 멘토링 팀 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentoring_team_id")
    private MentoringTeam mentoringTeam;  // 멘토링 팀 ID (주인)

    @Builder
    public MentoringBoard(Long id, String title, String contents, MentoringRole role, Integer mentoringCnt, String link, MentoringTeam mentoringTeam) {
        this.id = id;
        this.title = title;
        this.contents = contents;
        this.role = role;
        this.mentoringCnt = mentoringCnt;
        this.link = link;
        this.mentoringTeam = mentoringTeam;
    }


    public void setLink(String link) {
        this.link = link;
    }


    public RsSpecBoardDto toDto(MentoringTeam mentoringTeam) {
        RsSpecBoardDto dto = RsSpecBoardDto.builder()
                .id(this.getId())
                .title(this.getTitle())
                .mentoringTeamName(mentoringTeam.getName())
                .deadLine(mentoringTeam.getDeadline())
                .startDate(mentoringTeam.getStartDate())
                .endDate(mentoringTeam.getEndDate())
                .role(this.getRole())
                .mentoringCnt(this.getMentoringCnt())
                .contents(this.getContents())
                .createdDate(this.getCreatedDate())
                .modifiedDate(this.getLastModifiedDate())
                .link(this.getLink())
                .build();
        return dto;
    }

    public void updateBoard(RqBoardDto dto) {
        this.title = dto.getTitle();
        this.role = dto.getRole();
        this.mentoringCnt = dto.getMentoringCnt();
        this.link = dto.getLink();
        this.contents = dto.getContents();
    }

    /**
     * 연관관계 편의 메서드
     */
    public void addMentoringBoard(MentoringTeam mentoringTeam) {
        this.mentoringTeam = mentoringTeam;
        mentoringTeam.getMentoringBoardList().add(this);
    }

}