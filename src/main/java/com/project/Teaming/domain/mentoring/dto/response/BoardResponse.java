package com.project.Teaming.domain.mentoring.dto.response;

import com.project.Teaming.domain.mentoring.entity.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class BoardResponse {

    private Long boardId;
    private String title;
    private String mentoringTeamName;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> category;
    private String contents;
    private PostStatus status;

    public BoardResponse(Long boardId, String title, String mentoringTeamName, LocalDate startDate, LocalDate endDate, List<String> category, String contents, PostStatus status) {
        this.boardId = boardId;
        this.title = title;
        this.mentoringTeamName = mentoringTeamName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.category = category;
        this.contents = contents;
        this.status = status;
    }

    public BoardResponse(Long boardId, String title, String mentoringTeamName, LocalDate startDate, LocalDate endDate, String contents, PostStatus status) {
        this.boardId = boardId;
        this.title = title;
        this.mentoringTeamName = mentoringTeamName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.contents = contents;
        this.status = status;
    }

    public static BoardResponse from(MentoringBoard mentoringBoard, MentoringTeam mentoringTeam, List<String> categories) {
        BoardResponse dto = new BoardResponse();
        dto.setBoardId(mentoringBoard.getId());
        dto.setTitle(mentoringBoard.getTitle());
        dto.setMentoringTeamName(mentoringTeam.getName());
        dto.setStartDate(mentoringTeam.getStartDate());
        dto.setEndDate(mentoringTeam.getEndDate());
        dto.setCategory(categories);
        dto.setContents(mentoringBoard.getContents());
        dto.setStatus(mentoringBoard.getStatus());
        return dto;
    }
}
