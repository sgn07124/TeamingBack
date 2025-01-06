package com.project.Teaming.domain.mentoring.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.Teaming.domain.mentoring.entity.MentoringAuthority;
import com.project.Teaming.domain.mentoring.entity.MentoringRole;
import com.project.Teaming.domain.mentoring.entity.PostStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BoardSpecResponse {

    private MentoringAuthority authority;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isParticipate;
    private Long boardId;
    private Long teamId;
    private String title;
    private String mentoringTeamName;
    private LocalDate deadLine;
    private PostStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private MentoringRole role;
    private int mentoringCnt;
    private String link;
    private List<String> category;
    private String contents;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;


    @Builder
    public BoardSpecResponse(MentoringAuthority authority, Boolean isParticipate, Long boardId, Long teamId, String title, String mentoringTeamName, LocalDate deadLine, PostStatus status, LocalDate startDate, LocalDate endDate, MentoringRole role, int mentoringCnt, String link, List<String> category, String contents, LocalDateTime createdDate, LocalDateTime modifiedDate) {
        this.authority = authority;
        this.isParticipate = isParticipate;
        this.boardId = boardId;
        this.teamId = teamId;
        this.title = title;
        this.mentoringTeamName = mentoringTeamName;
        this.deadLine = deadLine;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.role = role;
        this.mentoringCnt = mentoringCnt;
        this.link = link;
        this.category = category;
        this.contents = contents;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
    }
}
