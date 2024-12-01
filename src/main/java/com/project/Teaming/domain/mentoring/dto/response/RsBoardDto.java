package com.project.Teaming.domain.mentoring.dto.response;

import com.project.Teaming.domain.mentoring.entity.MentoringAuthority;
import com.project.Teaming.domain.mentoring.entity.MentoringRole;
import lombok.Builder;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class RsBoardDto {

    private Long id;
    private String title;
    private String mentoringTeamName;
    private String startDate;
    private String endDate;
    private List<String> category;
    private String contents;

    @Builder
    public RsBoardDto(Long id, String title, String mentoringTeamName, String startDate, String endDate, List<String> category, String contents) {
        this.id = id;
        this.title = title;
        this.mentoringTeamName = mentoringTeamName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.category = category;
        this.contents = contents;
    }

    public RsBoardDto(Long id, String title, String mentoringTeamName, String startDate, String endDate, String category, String contents) {
        this.id = id;
        this.title = title;
        this.mentoringTeamName = mentoringTeamName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.category = Arrays.asList(category.split(","));
        this.contents = contents;
    }
}
