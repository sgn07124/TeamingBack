package com.project.Teaming.domain.mentoring.dto.response;

import com.project.Teaming.domain.mentoring.entity.MentoringStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class RsTeamDto {

    private Long id;
    private String name;  // 멘토링 명
    private LocalDate deadline;
    private LocalDate startDate;  // 멘토링 시작일
    private LocalDate endDate;  // 멘토링 종료일
    private int mentoringCnt;
    private String content;
    private MentoringStatus status;
    private String link;
    private List<String> categories;

    @Builder
    public RsTeamDto(Long id, String name, LocalDate deadline, LocalDate startDate, LocalDate endDate, int mentoringCnt, String content, MentoringStatus status, String link, List<String> categories) {
        this.id = id;
        this.name = name;
        this.deadline = deadline;
        this.startDate = startDate;
        this.endDate = endDate;
        this.mentoringCnt = mentoringCnt;
        this.content = content;
        this.status = status;
        this.link = link;
        this.categories = categories;
    }
}
