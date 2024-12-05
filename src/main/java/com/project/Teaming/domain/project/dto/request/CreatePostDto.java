package com.project.Teaming.domain.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class CreatePostDto {

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    @NotNull(message = "모집 마감일을 작성해 주세요")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String deadline;  // 모집 마감일

    @NotNull(message = "모집 인원을 입력해주세요.")
    private int memberCnt;

    private String link;  // 연락 방법

    @NotBlank(message = "모집 소개를 입력해주세요.")
    private String contents;
}
