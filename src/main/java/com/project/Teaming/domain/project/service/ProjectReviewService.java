package com.project.Teaming.domain.project.service;

import com.project.Teaming.domain.project.dto.request.ReviewDto;
import com.project.Teaming.domain.project.entity.ProjectParticipation;
import com.project.Teaming.domain.project.entity.ProjectStatus;
import com.project.Teaming.domain.project.repository.ProjectParticipationRepository;
import com.project.Teaming.domain.user.entity.Review;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.ReviewRepository;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjectReviewService {

    private final ProjectParticipationRepository projectParticipationRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    private User getLoginUser() {
        return userRepository.findById(getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
    }

    private Long getCurrentId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        return securityUser.getUserId();
    }

    private ProjectParticipation getReviewParticipationInfo(Long teamId, Long reviewUserId) {
        return projectParticipationRepository.findByProjectTeamIdAndUserId(teamId, reviewUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_PARTICIPATION));
    }

    public void reviewUser(ReviewDto dto) {
        // 로그인 사용자 조회(리뷰 작성자)
        User reviewer = getLoginUser();

        // 리뷰 작성자의 참여 정보 조회
        ProjectParticipation reviewerParticipation = getReviewParticipationInfo(dto.getTeamId(), reviewer.getId());

        // 리뷰 대상의 참여 정보 조회
        ProjectParticipation revieweeParticipation = getReviewParticipationInfo(dto.getTeamId(), dto.getRevieweeId());

        validateProjectComplete(revieweeParticipation);
        validateNotSelfReview(reviewer, revieweeParticipation);
        validateDuplicateReview(reviewerParticipation, dto.getRevieweeId());
        Review review = Review.projectReview(reviewerParticipation, revieweeParticipation.getUser(), dto.getRating(), dto.getContent());
        reviewRepository.save(review);
    }

    /**
     * 본인에 대한 리뷰는 작성 불가
     */
    private void validateNotSelfReview(User reviewer, ProjectParticipation revieweeParticipation) {
        if (reviewer.getId().equals(revieweeParticipation.getUser().getId())) {
            throw new BusinessException(ErrorCode.INVALID_SELF_ACTION);
        }
    }

    /**
     * 프로젝트가 완료되지 않은 경우 에러 처리
     */
    private void validateProjectComplete(ProjectParticipation revieweeParticipation) {
        if (!revieweeParticipation.getProjectTeam().getStatus().equals(ProjectStatus.COMPLETE)) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_COMPLETE);
        }
    }

    /**
     * 중복 리뷰 방지
     */
    private void validateDuplicateReview(ProjectParticipation reviewerParticipation, Long revieweeId) {
        boolean isDuplicate = reviewRepository.existsByProjectParticipationAndRevieweeId(reviewerParticipation, revieweeId);
        if (isDuplicate) {
            throw new BusinessException(ErrorCode.ALREADY_REVIEWED);
        }
    }
}
