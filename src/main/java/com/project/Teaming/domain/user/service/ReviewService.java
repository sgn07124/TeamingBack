package com.project.Teaming.domain.user.service;

import com.project.Teaming.domain.mentoring.dto.response.TeamUserResponse;
import com.project.Teaming.domain.user.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    /**
     * 사용자에 대한 리뷰 상태를 설정합니다.
     */
    public void setReviewInfo(List<TeamUserResponse> users, Long currentParticipationId) {
        // 사용자 ID 목록 추출
        Set<Long> userIds = users.stream()
                .map(TeamUserResponse::getUserId)
                .collect(Collectors.toSet());

        // 리뷰 상태 조회
        Set<Long> reviewedUserIds = reviewRepository.findReviewedUserIds(currentParticipationId, userIds);
        // 리뷰 상태 설정
        users.forEach(user -> user.setIsReviewed(reviewedUserIds.contains(user.getUserId())));
    }
}