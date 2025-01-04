package com.project.Teaming.domain.user.controller;

import com.project.Teaming.domain.user.dto.response.ReviewDto;
import com.project.Teaming.global.result.ResultCode;
import com.project.Teaming.global.result.ResultListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/mock/users")
@RequiredArgsConstructor
public class UserMockController {


    @GetMapping("/reviews")
    public ResultListResponse<ReviewDto> getReviews() {
        List<ReviewDto> dtos = Arrays.asList(
                new ReviewDto(1L, "John Doe", "Great experience!", LocalDateTime.now().minusDays(1)),
                new ReviewDto(2L, "Jane Smith", "Very helpful and professional.", LocalDateTime.now().minusDays(2)),
                new ReviewDto(3L, "Chris Johnson", "Would recommend to others.", LocalDateTime.now().minusDays(3)),
                new ReviewDto(4L, "Patricia Lee", "Excellent service!", LocalDateTime.now().minusDays(4)),
                new ReviewDto(5L, "Michael Brown", "Quick and efficient.", LocalDateTime.now().minusDays(5))
        );
        return new ResultListResponse<>(ResultCode.GET_USER_REVIEWS, dtos);
    }

    @GetMapping("/{userId}/reviews")
    public ResultListResponse<ReviewDto> getUserReviews(@PathVariable Long userId) {
        // Mock 데이터 생성
        List<ReviewDto> userReviews = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            userReviews.add(new ReviewDto(
                    (long) i, // reviewerId는 각기 다른 값으로 설정
                    "Reviewer" + i, // 각 리뷰어의 이름
                    "Review content " + i + " for User " + userId, // 리뷰 내용
                    LocalDateTime.now().minusDays(i) // 생성 날짜
            ));
        }
        return new ResultListResponse<>(ResultCode.GET_USER_REVIEWS, userReviews);
    }

}
