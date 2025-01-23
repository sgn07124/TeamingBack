package com.project.Teaming.domain.mentoring.dto.request;

import com.project.Teaming.domain.mentoring.entity.MentoringRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class BoardRequest {
    @NotBlank(message = "제목을 작성해주세요")
    private String title;  // 모집글 제목
    @NotNull(message = "모집하는 역할을 작성해주세요")
    private MentoringRole role;  // 모집하는 역할
    private int mentoringCnt;
    private String link;  // 연락 방법
    @NotNull(message = "모집 마감기한을 작성해주세요 ")
    private LocalDate deadLine;
    @NotBlank(message = "내용을 작성해주세요")
    private String contents;  // 모집글 내용
}