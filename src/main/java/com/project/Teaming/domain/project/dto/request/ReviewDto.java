package com.project.Teaming.domain.project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReviewDto {

    @NotNull
    private Long teamId;
    @NotNull(message = "리뷰 대상자 ID를 입력해 주세요.")
    private Long revieweeId;  // 리뷰 대상자 id
    @NotNull(message = "1~5로 입력해 주세요.")
    private int rating;
    @NotNull(message = "리뷰 내용을 입력해 주세요.")
    private String content;
}
