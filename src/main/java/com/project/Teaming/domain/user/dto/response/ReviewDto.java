package com.project.Teaming.domain.user.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReviewDto {
    private Long reviewerId;
    private String reviewerName;
    private String content;
    private int rate;
    private LocalDateTime createdDate;

    public ReviewDto(Long reviewerId, String reviewerName, String content, LocalDateTime createdDate,int rate) {
        this.reviewerId = reviewerId;
        this.reviewerName = reviewerName;
        this.content = content;
        this.createdDate = createdDate;
        this.rate = rate;
    }
}
