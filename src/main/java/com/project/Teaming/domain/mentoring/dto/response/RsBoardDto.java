package com.project.Teaming.domain.mentoring.dto.response;

import com.project.Teaming.domain.mentoring.entity.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
public class RsBoardDto {

    private Long id;
    private String title;
    private String mentoringTeamName;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> category;
    private String contents;
    private PostStatus status;

    @Builder
    public RsBoardDto(Long id, String title, String mentoringTeamName, LocalDate startDate, LocalDate endDate, List<String> category, String contents, PostStatus status) {
        this.id = id;
        this.title = title;
        this.mentoringTeamName = mentoringTeamName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.category = category;
        this.contents = contents;
        this.status = status;
    }


    public RsBoardDto(Long id, String title, String mentoringTeamName, LocalDate startDate, LocalDate endDate, String category, String contents) {
        this.id = id;
        this.title = title;
        this.mentoringTeamName = mentoringTeamName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.category = Arrays.asList(category.split(","));
        this.contents = contents;
    }

    public RsBoardDto(Long id, String title, String mentoringTeamName, LocalDate startDate, LocalDate endDate, String contents, PostStatus status) {
        this.id = id;
        this.title = title;
        this.mentoringTeamName = mentoringTeamName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.contents = contents;
        this.status = status;
    }

    public static RsBoardDto from(MentoringBoard mentoringBoard, MentoringTeam mentoringTeam, List<String> categories) {
        RsBoardDto dto = new RsBoardDto();
        dto.setId(mentoringBoard.getId());
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
