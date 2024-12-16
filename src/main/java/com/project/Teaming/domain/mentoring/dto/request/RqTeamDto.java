package com.project.Teaming.domain.mentoring.dto.request;

import com.project.Teaming.domain.mentoring.entity.MentoringRole;
import com.project.Teaming.domain.mentoring.entity.MentoringStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class RqTeamDto {

    @NotNull(message = "멘토링 팀 이름을 작성해 주세요")
    private String name;  // 멘토링 명
    @NotNull(message = "모집 마감일을 작성해 주세요")
    private LocalDate deadline;  // 모집 마감일
    @NotBlank(message = "멘토링 시작일을 작성해 주세요")
    private LocalDate startDate;  // 멘토링 시작일
    @NotBlank(message = "멘토링 종료일을 작성해 주세요")
    private LocalDate endDate;  // 멘토링 종료일
    private int mentoringCnt;
    @NotBlank(message = "멘토링 팀 설명을 작성해 주세요")
    private String content;
    private MentoringStatus status;
    @NotBlank(message = "연락방법을 작성해 주세요")
    private String link;
    @NotNull(message = "내 역할을 작성해 주세요")
    private MentoringRole role;
    @NotNull(message = "모집 카테고리를 작성해 주세요")
    private List<Long> categories;

    @Builder
    public RqTeamDto(String name, LocalDate deadline, LocalDate startDate, LocalDate endDate, int mentoringCnt, String content, MentoringStatus status, String link, MentoringRole role, List<Long> categories) {
        this.name = name;
        this.deadline = deadline;
        this.startDate = startDate;
        this.endDate = endDate;
        this.mentoringCnt = mentoringCnt;
        this.content = content;
        this.status = status;
        this.link = link;
        this.role = role;
        this.categories = categories;
    }
}
