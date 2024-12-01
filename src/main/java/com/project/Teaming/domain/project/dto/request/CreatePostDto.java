package com.project.Teaming.domain.project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePostDto {

    @NotNull(message = "제목을 입력해주세요.")
    private String title;

    @NotNull(message = "모집 마감일을 작성해 주세요")
    private String deadline;  // 모집 마감일

    @NotNull(message = "모집 인원을 입력해주세요.")
    private int memberCnt;

    private String link;  // 연락 방법

    @NotNull(message = "모집 소개를 입력해주세요.")
    private String contents;
}
