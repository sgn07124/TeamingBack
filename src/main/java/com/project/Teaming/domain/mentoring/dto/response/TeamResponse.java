package com.project.Teaming.domain.mentoring.dto.response;

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

    private Long id;
    private String name;  // 멘토링 명
    private LocalDate startDate;  // 멘토링 시작일
    private LocalDate endDate;  // 멘토링 종료일
    private int mentoringCnt;
    private String content;
    private MentoringStatus status;
    private String link;
    private List<String> categories;

    public TeamResponse(Long id, String name, LocalDate startDate, LocalDate endDate, int mentoringCnt, String content, MentoringStatus status, String link, List<String> categories) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.mentoringCnt = mentoringCnt;
        this.content = content;
        this.status = status;
        this.link = link;
        this.categories = categories;
    }

    public static TeamResponse from(MentoringTeam mentoringTeam) {
        TeamResponse dto = new TeamResponse();
        dto.setId(mentoringTeam.getId());
        dto.setName(mentoringTeam.getName());
        dto.setStartDate(mentoringTeam.getStartDate());
        dto.setEndDate(mentoringTeam.getEndDate());
        dto.setMentoringCnt(mentoringTeam.getMentoringCnt());
        dto.setContent(mentoringTeam.getContent());
        dto.setStatus(mentoringTeam.getStatus());
        dto.setLink(mentoringTeam.getLink());
        return dto;
    }
}
