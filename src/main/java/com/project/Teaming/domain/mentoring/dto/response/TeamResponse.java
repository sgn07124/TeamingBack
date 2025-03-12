package com.project.Teaming.domain.mentoring.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.Teaming.domain.mentoring.entity.MentoringStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private String teamName;  // 멘토링 명
    private LocalDate startDate;  // 멘토링 시작일
    private LocalDate endDate;  // 멘토링 종료일
    private int mentoringCnt;
    private String content;
    private String createdDate;
    private MentoringStatus status;
    private String link;
    private List<String> categories;


    public static TeamResponse from(MentoringTeam mentoringTeam) {
        TeamResponse dto = new TeamResponse();
        dto.setId(mentoringTeam.getId());
        dto.setTeamName(mentoringTeam.getName());
        dto.setStartDate(mentoringTeam.getStartDate());
        dto.setCreatedDate(String.valueOf(mentoringTeam.getCreatedDate()));
        dto.setEndDate(mentoringTeam.getEndDate());
        dto.setMentoringCnt(mentoringTeam.getMentoringCnt());
        dto.setContent(mentoringTeam.getContent());
        dto.setStatus(mentoringTeam.getStatus());
        dto.setLink(mentoringTeam.getLink());
        return dto;
    }
}
