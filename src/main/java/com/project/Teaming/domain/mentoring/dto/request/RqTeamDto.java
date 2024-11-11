package com.project.Teaming.domain.mentoring.dto.request;

import com.project.Teaming.domain.mentoring.entity.MentoringStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
public class RqTeamDto {

    @NotNull
    private String name;  // 멘토링 명
    @NotBlank
    private String startDate;  // 멘토링 시작일
    @NotBlank
    private String endDate;  // 멘토링 종료일
    private int mentoringCnt;
    @NotBlank
    private String content;
    private MentoringStatus status;
    @NotBlank
    private String link;

    @Builder
    public RqTeamDto(String name, String startDate, String endDate, int mentoringCnt, String content, MentoringStatus status, String link) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.mentoringCnt = mentoringCnt;
        this.content = content;
        this.status = status;
        this.link = link;
    }
}
