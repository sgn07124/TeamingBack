package com.project.Teaming.domain.mentoring.dto.request;

import com.project.Teaming.domain.mentoring.entity.MentoringRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RqBoardDto {
    @NotBlank(message = "제목을 작성해주세요")
    private String title;  // 모집글 제목
    @NotBlank(message = "모집하는 역할을 작성해주세요")
    private MentoringRole role;  // 모집하는 역할
    private int mentoringCnt;
    private String link;  // 연락 방법
    @NotBlank(message = "내용을 작성해주세요")
    private String contents;  // 모집글 내용
}