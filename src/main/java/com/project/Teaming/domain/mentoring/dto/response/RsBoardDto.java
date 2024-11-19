package com.project.Teaming.domain.mentoring.dto.response;

import com.project.Teaming.domain.mentoring.entity.MentoringBoard;
import com.project.Teaming.domain.mentoring.entity.MentoringRole;
import com.project.Teaming.domain.mentoring.entity.MentoringStatus;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RsBoardDto {

    private Long id;
    private String title;
    private String contents;
    private MentoringRole role;
    private String link;
    private MentoringStatus status;
    private Long mentoringTeamId;
    private String startDate;
    private String endDate;

    @Builder
    public RsBoardDto(Long id, String title, String contents, MentoringRole role, String link, MentoringStatus status, Long mentoringTeamId, String startDate, String endDate) {
        this.id = id;
        this.title = title;
        this.contents = contents;
        this.role = role;
        this.link = link;
        this.status = status;
        this.mentoringTeamId = mentoringTeamId;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
