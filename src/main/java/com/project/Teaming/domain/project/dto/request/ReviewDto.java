package com.project.Teaming.domain.project.dto.request;

import lombok.Data;

@Data
public class ReviewDto {

    private Long teamId;
    private Long revieweeId;  // 리뷰 대상자 id
    private int rating;
    private String content;
}
