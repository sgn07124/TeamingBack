package com.project.Teaming.domain.mentoring.dto.response;

import com.project.Teaming.domain.mentoring.entity.MentoringStatus;
import lombok.Builder;
import lombok.Data;

@Data
public class RsTeamDto {

    private Long id;
    private String name;  // 멘토링 명
    private String startDate;  // 멘토링 시작일
    private String endDate;  // 멘토링 종료일
    private int mentoringCnt;
    private String content;
    private MentoringStatus status;
    private String link;

    @Builder
    public RsTeamDto(Long id, String name, String startDate, String endDate, int mentoringCnt, String content, MentoringStatus status, String link) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.mentoringCnt = mentoringCnt;
        this.content = content;
        this.status = status;
        this.link = link;
    }
}
