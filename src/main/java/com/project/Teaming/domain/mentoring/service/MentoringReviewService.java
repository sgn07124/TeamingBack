package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.dto.request.MentoringReviewRequest;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.mentoring.repository.MentoringParticipationRepository;
import com.project.Teaming.domain.mentoring.repository.MentoringTeamRepository;
import com.project.Teaming.domain.user.entity.Review;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.ReviewRepository;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.error.exception.MentoringTeamNotFoundException;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MentoringReviewService {

    private final UserRepository userRepository;
    private final MentoringParticipationRepository mentoringParticipationRepository;
    private final ReviewRepository reviewRepository;
    private final MentoringTeamRepository mentoringTeamRepository;

    @Transactional
    public void review(MentoringReviewRequest dto) {
        User user = getUser();
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(dto.getTeamId()).orElseThrow(MentoringTeamNotFoundException::new);
        MentoringParticipation reviewingParticipation = validateReviewingParticipation(user,mentoringTeam);
        User reviewedUser = validateReviewedUser(dto.getReviewedUserId());
        validateReviewedParticipation(mentoringTeam, reviewedUser);

        // 팀 상태 검증
        validateMentoringTeamStatus(mentoringTeam);
        // 중복 리뷰 검증
        validateDuplicateReview(reviewingParticipation, reviewedUser);
        // 자기자신에 대해서 쓰는지 검증
        validateSelfReview(reviewingParticipation.getUser(), reviewedUser);

        Review review = Review.mentoringReview(reviewingParticipation, reviewedUser, dto.getRate(), dto.getContent());
        reviewRepository.save(review);
    }

    private MentoringParticipation validateReviewingParticipation(User user,MentoringTeam mentoringTeam) {
        return mentoringParticipationRepository.findDynamicMentoringParticipation(
                mentoringTeam,user,null, MentoringParticipationStatus.ACCEPTED,null,false)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_A_MEMBER));
    }

    private User validateReviewedUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }

    private void validateReviewedParticipation(MentoringTeam mentoringTeam, User reviewedUser) {
        mentoringParticipationRepository.findDynamicMentoringParticipation(
                        mentoringTeam, reviewedUser, null, null, List.of(MentoringParticipationStatus.ACCEPTED, MentoringParticipationStatus.EXPORT), null)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REVIEW_TARGET));
    }

    private void validateMentoringTeamStatus(MentoringTeam mentoringTeam) {
        if (mentoringTeam.getStatus() != MentoringStatus.COMPLETE) {
            throw new BusinessException(ErrorCode.STILL_IN_PROGRESS);
        }
    }

    private void validateDuplicateReview(MentoringParticipation reviewingParticipation, User reviewedUser) {
        if (reviewRepository.existsByMentoringParticipationAndReviewee(reviewingParticipation, reviewedUser)) {
            throw new BusinessException(ErrorCode.ALREADY_REVIEWED);
        }
    }

    private void validateSelfReview(User reviewer, User reviewee) {
        if (reviewer.equals(reviewee)) {
            throw new BusinessException(ErrorCode.INVALID_SELF_ACTION);
        }
    }
    private User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        Long userId = securityUser.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        return user;
    }
}
