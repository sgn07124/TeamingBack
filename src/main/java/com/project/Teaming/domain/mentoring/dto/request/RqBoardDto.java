package com.project.Teaming.domain.mentoring.dto.request;

import com.project.Teaming.domain.mentoring.entity.MentoringRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RqBoardDto {

    @NotBlank
    private String title;  // 모집글 제목
    @NotBlank
    private String contents;  // 모집글 내용
    @NotBlank
    private MentoringRole role;  // 모집하는 역할
    @NotBlank
    private String startDate;
    @NotBlank
    private String endDate;
    private String link;  // 연락 방법
}
