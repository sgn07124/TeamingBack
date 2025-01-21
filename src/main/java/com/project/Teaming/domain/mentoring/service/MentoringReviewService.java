package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.dto.request.MentoringReviewRequest;
import com.project.Teaming.domain.mentoring.dto.response.TeamUserResponse;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.mentoring.provider.MentoringParticipationDataProvider;
import com.project.Teaming.domain.mentoring.provider.MentoringTeamDataProvider;
import com.project.Teaming.domain.mentoring.provider.UserDataProvider;
import com.project.Teaming.domain.mentoring.repository.MentoringParticipationRepository;
import com.project.Teaming.domain.mentoring.repository.MentoringTeamRepository;
import com.project.Teaming.domain.mentoring.service.policy.MentoringReviewPolicy;
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

    private final UserDataProvider userDataProvider;
    private final MentoringReviewPolicy mentoringReviewPolicy;
    private final MentoringParticipationDataProvider mentoringParticipationDataProvider;
    private final ReviewRepository reviewRepository;
    private final MentoringTeamDataProvider mentoringTeamDataProvider;

    @Transactional
    public void review(MentoringReviewRequest dto) {
        User user = userDataProvider.getUser();

        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(dto.getTeamId());

        MentoringParticipation reviewingParticipation = mentoringParticipationDataProvider.findParticipationWith(mentoringTeam,user,null,
                MentoringParticipationStatus.ACCEPTED,null,() -> new BusinessException(ErrorCode.NOT_A_MEMBER));
        User reviewedUser = userDataProvider.findUser(dto.getReviewedUserId());

        mentoringReviewPolicy.validateToReview(mentoringTeam,reviewedUser,reviewingParticipation);

        Review review = Review.mentoringReview(reviewingParticipation, reviewedUser, dto.getRate(), dto.getContent());
        reviewRepository.save(review);
    }

}
