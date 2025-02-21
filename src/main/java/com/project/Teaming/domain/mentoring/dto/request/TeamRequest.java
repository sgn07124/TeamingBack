package com.project.Teaming.domain.mentoring.dto.request;

import com.project.Teaming.domain.mentoring.entity.MentoringRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class TeamRequest {

    @NotNull(message = "멘토링 팀 이름을 작성해 주세요")
    private String name;  // 멘토링 명
    @NotNull(message = "멘토링 시작일을 작성해 주세요")
    private LocalDate startDate;  // 멘토링 시작일
    @NotNull(message = "멘토링 종료일을 작성해 주세요")
    private LocalDate endDate;  // 멘토링 종료일
    private int mentoringCnt;
    @NotBlank(message = "멘토링 팀 설명을 작성해 주세요")
    private String content;
    @NotBlank(message = "연락방법을 작성해 주세요")
    private String link;
    @NotNull(message = "내 역할을 작성해 주세요")
    private MentoringRole role;
    @NotNull(message = "모집 카테고리를 작성해 주세요")
    private List<Long> categories;

    public TeamRequest(String name, LocalDate startDate, LocalDate endDate, int mentoringCnt, String content, String link, MentoringRole role, List<Long> categories) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.mentoringCnt = mentoringCnt;
        this.content = content;
        this.link = link;
        this.role = role;
        this.categories = categories;
    }
}
