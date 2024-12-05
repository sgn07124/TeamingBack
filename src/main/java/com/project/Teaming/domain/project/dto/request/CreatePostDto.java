package com.project.Teaming.domain.project.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    @Min(value = 1, message = "모집 인원은 최소 1명이어야 합니다.")
    @Max(value = 20, message = "모집 인원은 최대 20명까지 가능합니다.")
    @Digits(integer = 2, fraction = 0, message = "모집 인원은 정수만 입력 가능합니다.")
    private int memberCnt;

    private String link;  // 연락 방법

    @NotBlank(message = "모집 소개를 입력해주세요.")
    private String contents;
}
