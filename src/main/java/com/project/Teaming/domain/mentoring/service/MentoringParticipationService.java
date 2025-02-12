package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.annotation.NotifyAfterTransaction;
import com.project.Teaming.domain.mentoring.dto.request.ParticipationRequest;
import com.project.Teaming.domain.mentoring.dto.response.*;
import com.project.Teaming.domain.mentoring.entity.*;
import com.project.Teaming.domain.mentoring.provider.MentoringParticipationDataProvider;
import com.project.Teaming.domain.mentoring.provider.MentoringTeamDataProvider;
import com.project.Teaming.domain.mentoring.provider.UserDataProvider;
import com.project.Teaming.domain.mentoring.repository.MentoringParticipationRepository;
import com.project.Teaming.domain.mentoring.service.policy.MentoringParticipationPolicy;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.ReportRepository;
import com.project.Teaming.domain.user.service.ReportService;
import com.project.Teaming.domain.user.service.ReviewService;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.*;
import com.project.Teaming.global.sse.service.NotificationService;
import com.project.Teaming.global.sse.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentoringParticipationService {

    private final MentoringParticipationRepository mentoringParticipationRepository;
    private final MentoringParticipationDataProvider mentoringParticipationDataProvider;
    private final MentoringParticipationPolicy mentoringParticipationPolicy;
    private final MentoringTeamDataProvider mentoringTeamDataProvider;
    private final UserDataProvider userDataProvider;
    private final RedisTeamUserManagementService redisTeamUserManagementService;
    private final RedisApplicantManagementService redisApplicantManagementService;
    private final ReviewService reviewService;
    private final ReportService reportService;
    private final MentoringNotificationService mentoringNotificationService;

    /**
     * 지원자로 등록하는 로직
     * @param dto
     * @return
     */

    @Transactional
    public MentoringParticipation saveLeader(Long teamId, ParticipationRequest dto) {
        User user = userDataProvider.getUser();
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(teamId);
        Optional<MentoringParticipation> participation = mentoringParticipationRepository.findDynamicMentoringParticipation(
                mentoringTeam, user,null,null);

        participation.ifPresent(mentoringParticipationPolicy::validateParticipationStatus);
        MentoringParticipation mentoringParticipation = MentoringParticipation.from(dto);
        mentoringParticipation.setDecisionDate(LocalDateTime.now());

        mentoringParticipation.setUser(user);
        mentoringParticipation.addMentoringTeam(mentoringTeam);
        MentoringParticipation savedParticipation = mentoringParticipationRepository.save(mentoringParticipation);

        return savedParticipation;
    }

    @Transactional
    public MentoringParticipation saveMentoringParticipation(MentoringBoard post, ParticipationRequest dto) {
        User user = userDataProvider.getUser();
        MentoringTeam mentoringTeam = post.getMentoringTeam();
        Optional<MentoringParticipation> participation = mentoringParticipationRepository.findDynamicMentoringParticipation(
                mentoringTeam, user,null,null);

        participation.ifPresent(mentoringParticipationPolicy::validateParticipationStatus);
        MentoringParticipation mentoringParticipation = MentoringParticipation.from(dto);
        mentoringParticipation.setUser(user);
        mentoringParticipation.addMentoringTeam(mentoringTeam);
        MentoringParticipation savedParticipation = mentoringParticipationRepository.save(mentoringParticipation);
        TeamParticipationResponse participationDto = TeamParticipationResponse.toParticipationDto(savedParticipation);
        redisApplicantManagementService.saveApplicantWithTTL(mentoringTeam.getId(),participationDto,post.getDeadLine());
        // 알림
        mentoringNotificationService.participate(user.getId(),mentoringTeam.getId());
        return savedParticipation;
    }

    /**
     * 지원 취소하는 로직
     * @param mentoringTeamId
     */
    @Transactional
    public void cancelMentoringParticipation(Long teamId) {
        User user = userDataProvider.getUser();
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(teamId);

        MentoringParticipation participation = mentoringParticipationDataProvider.findParticipationWith(
                mentoringTeam, user,null,null,() -> new BusinessException(ErrorCode.MENTORING_PARTICIPATION_NOT_EXIST));

        mentoringParticipationPolicy.validateCancellation(participation);
        redisApplicantManagementService.removeApplicant(teamId,String.valueOf(user.getId()));
        removeParticipant(participation,user,mentoringTeam);
    }

    /**
     * 지원 수락 로직
     * @param team_Id
     * @param participant_id
     */
    @NotifyAfterTransaction
    @Transactional
    public List<Long> acceptMentoringParticipation(Long teamId, Long participantId) {
        User user = userDataProvider.getUser();
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(teamId);

        mentoringParticipationPolicy.validateParticipation(
                mentoringTeam, user, MentoringAuthority.LEADER ,MentoringParticipationStatus.ACCEPTED,
                 () -> new BusinessException(ErrorCode.NOT_A_LEADER));

        MentoringParticipation mentoringParticipation = mentoringParticipationDataProvider.findParticipation(participantId);
        //검증
        mentoringParticipationPolicy.validateParticipationStatusForAcceptance(mentoringParticipation);
        Long acceptParticipationId = mentoringParticipation.getUser().getId();
        //수락 처리
        mentoringParticipation.accept();
        redisApplicantManagementService.updateApplicantStatus(teamId,String.valueOf(mentoringParticipation.getUser().getId()),MentoringParticipationStatus.ACCEPTED);
        return mentoringNotificationService.accept(acceptParticipationId, teamId);
    }

    /**
     * 지원 거절 로직
     * @param teamId
     * @param participant_id
     */
    @NotifyAfterTransaction
    @Transactional
    public List<Long> rejectMentoringParticipation(Long teamId, Long participantId) {
        User user = userDataProvider.getUser();
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(teamId);

        mentoringParticipationPolicy.validateParticipation(
                mentoringTeam, user, MentoringAuthority.LEADER, MentoringParticipationStatus.ACCEPTED,
                 () -> new BusinessException(ErrorCode.NOT_A_LEADER));

        MentoringParticipation rejectParticipation = mentoringParticipationDataProvider.findParticipation(participantId);
        Long rejectedUserId = rejectParticipation.getUser().getId();

        mentoringParticipationPolicy.validateParticipationStatusForAcceptance(rejectParticipation);
        //거절처리
        redisApplicantManagementService.updateApplicantStatus(teamId,String.valueOf(rejectParticipation.getUser().getId()),MentoringParticipationStatus.REJECTED);
        // DB에서 지원자 데이터 삭제
        removeParticipant(rejectParticipation, rejectParticipation.getUser(), mentoringTeam);
        return mentoringNotificationService.reject(rejectedUserId,teamId);
    }

    /**
     * 강퇴하는 로직
     * @param teamId
     * @param userId
     */

    @NotifyAfterTransaction
    @Transactional
    public List<Long> exportTeamUser(Long teamId, Long userId) {
        User user = userDataProvider.getUser();
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(teamId);
        User exportUser = userDataProvider.findUser(userId);

        mentoringParticipationPolicy.validateParticipation(
                mentoringTeam, user, MentoringAuthority.LEADER,MentoringParticipationStatus.ACCEPTED,
                 () -> new BusinessException(ErrorCode.NOT_A_LEADER));

        MentoringParticipation export = mentoringParticipationDataProvider.findParticipationWith(
                mentoringTeam, exportUser,MentoringAuthority.CREW, MentoringParticipationStatus.ACCEPTED,
                () -> new BusinessException(ErrorCode.EXPORTED_MEMBER_NOT_EXISTS));
        // 강퇴
        TeamUserResponse exportedParticipation = export.export();
        redisTeamUserManagementService.saveParticipation(mentoringTeam.getId(), exportUser.getId(), exportedParticipation);
        removeTeamUser(export,exportUser,mentoringTeam);
        return mentoringNotificationService.export(userId,teamId);
    }

    /**
     * 탈퇴하는 로직
     * @param teamId
     */
    @NotifyAfterTransaction
    @Transactional
    public List<Long> deleteUser(Long teamId) {
        User user = userDataProvider.getUser();

        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(teamId);

        MentoringParticipation teamUser = mentoringParticipationDataProvider.findParticipationWith(
                mentoringTeam, user,null,MentoringParticipationStatus.ACCEPTED,
                 () -> new BusinessException(ErrorCode.NOT_A_MEMBER));

        if (teamUser.getAuthority() == MentoringAuthority.LEADER) {
            //제일 일찍 들어온 팀원 조회
            Optional<MentoringParticipation> firstMember = mentoringParticipationRepository.findFirstUser(
                    mentoringTeam.getId(), MentoringParticipationStatus.ACCEPTED, MentoringAuthority.CREW);
            // 새로운 리더 설정
            firstMember.ifPresentOrElse(
                    MentoringParticipation::setLeader,
                    () -> {
                        throw new BusinessException(ErrorCode.NO_ELIGIBLE_MEMBER_FOR_LEADER);
                    }
            );
        }
        TeamUserResponse teamUserResponse = teamUser.deleteParticipant();
        redisTeamUserManagementService.saveParticipation(mentoringTeam.getId(), user.getId(),teamUserResponse);
        removeTeamUser(teamUser,user,mentoringTeam);
        return mentoringNotificationService.delete(user.getId(),teamId);
    }

    /**
     * 권한별로 팀원과 지원자 데이터를 반환하는 로직
     * 리더용(팀원, 지원자현황)
     * 팀원용(팀원)
     * @param teamId
     * @return
     */
    @Transactional(readOnly = true)
    public ParticipantsResponse<?> getParticipantsInfo(Long teamId) {
        User user = userDataProvider.getUser();

        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(teamId);

        MentoringParticipation currentParticipation = mentoringParticipationDataProvider.findParticipationWith(
                mentoringTeam, user, null, MentoringParticipationStatus.ACCEPTED,
                () -> new BusinessException(ErrorCode.NOT_A_MEMBER));

        List<TeamUserResponse> teamUsers = mentoringParticipationRepository.findAllByMemberStatus(
                mentoringTeam, MentoringStatus.COMPLETE, MentoringParticipationStatus.ACCEPTED, currentParticipation.getId());
        setLoginStatus(teamUsers, user.getId()); // 로그인 상태 설정


        List<TeamUserResponse> deletedOrExportedUsers = redisTeamUserManagementService.getDeletedOrExportedParticipations(teamId);
        if (deletedOrExportedUsers != null && !deletedOrExportedUsers.isEmpty()) {
            reviewService.setReviewInfo(deletedOrExportedUsers, currentParticipation.getId()); // 리뷰 여부 검증
            reportService.setReportInfo(deletedOrExportedUsers, currentParticipation.getId()); // 신고 여부 검증
        }
        List<TeamUserResponse> combineUsers = TeamUserResponse.combine(teamUsers, deletedOrExportedUsers);

        switch (currentParticipation.getAuthority()) {
            case LEADER:
                List<TeamParticipationResponse> applicants = redisApplicantManagementService.getApplicants(teamId);
                return new ParticipantsResponse<>(mentoringTeam.getId(), MentoringAuthority.LEADER, new ForLeaderResponse(combineUsers, applicants));

            case CREW:
                return new ParticipantsResponse<>(mentoringTeam.getId(), MentoringAuthority.CREW, combineUsers);

            default:
                throw new BusinessException(ErrorCode.NOT_A_MEMBER);
        }
    }


    private void setLoginStatus(List<?> dtos, Long userId) {
        dtos.forEach(dto -> {
            if (dto instanceof TeamUserResponse teamDto && teamDto.getUserId().equals(userId)) {
                teamDto.setIsLogined(true);
            } else if (dto instanceof ParticipationForUserResponse userDto && userDto.getUserId().equals(userId)) {
                userDto.setIsLogined(true);
            }
        });
    }

    private void removeParticipant(MentoringParticipation mentoringParticipation,User user, MentoringTeam mentoringTeam) {
        mentoringParticipation.removeMentoringTeam(mentoringTeam);
        mentoringParticipation.removeUser(user);
        mentoringParticipationRepository.delete(mentoringParticipation);
    }

    private void removeTeamUser(MentoringParticipation mentoringParticipation,User user, MentoringTeam mentoringTeam) {
        mentoringParticipation.removeMentoringTeam(mentoringTeam);
        mentoringParticipation.removeUser(user);
        reportService.deleteAllReportsForMentoringParticipation(mentoringParticipation);
        mentoringParticipationRepository.delete(mentoringParticipation);
    }

}
