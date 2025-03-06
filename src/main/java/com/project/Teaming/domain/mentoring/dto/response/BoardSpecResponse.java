package com.project.Teaming.domain.mentoring.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.Teaming.domain.mentoring.entity.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardSpecResponse {

    private MentoringAuthority authority;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isParticipate;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long boardId;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
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

    public static BoardSpecResponse from(MentoringBoard mentoringBoard,MentoringTeam mentoringTeam) {
        BoardSpecResponse dto = new BoardSpecResponse();
        dto.setBoardId(mentoringBoard.getId());
        dto.setTeamId(mentoringTeam.getId());
        dto.setTitle(mentoringBoard.getTitle());
        dto.setMentoringTeamName(mentoringTeam.getName());
        dto.setDeadLine(mentoringBoard.getDeadLine());
        dto.setStartDate(mentoringTeam.getStartDate());
        dto.setEndDate(mentoringTeam.getEndDate());
        dto.setStatus(mentoringBoard.getStatus());
        dto.setRole(mentoringBoard.getRole());
        dto.setMentoringCnt(mentoringBoard.getMentoringCnt());
        dto.setContents(mentoringBoard.getContents());
        dto.setCreatedDate(mentoringBoard.getCreatedDate());
        dto.setModifiedDate(mentoringBoard.getLastModifiedDate());
        dto.setLink(mentoringBoard.getLink());
        return dto;
    }
}
