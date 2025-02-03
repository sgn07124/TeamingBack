package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.dto.request.ParticipationRequest;
import com.project.Teaming.domain.mentoring.dto.response.*;
import com.project.Teaming.domain.mentoring.entity.*;
import com.project.Teaming.domain.mentoring.provider.MentoringParticipationDataProvider;
import com.project.Teaming.domain.mentoring.provider.MentoringTeamDataProvider;
import com.project.Teaming.domain.mentoring.provider.UserDataProvider;
import com.project.Teaming.domain.mentoring.repository.MentoringParticipationRepository;
import com.project.Teaming.domain.mentoring.service.policy.MentoringParticipationPolicy;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.service.ReportService;
import com.project.Teaming.domain.user.service.ReviewService;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.*;
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
    private final RedisTeamUserManagementService redisParticipationManagementService;
    private final RedisApplicantManagementService redisApplicantManagementService;
    private final AsyncParticipationService asyncParticipationService;

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
    @Transactional
    public void acceptMentoringParticipation(Long teamId, Long participantId) {
        User user = userDataProvider.getUser();
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(teamId);

        mentoringParticipationPolicy.validateParticipation(
                mentoringTeam, user, MentoringAuthority.LEADER ,MentoringParticipationStatus.ACCEPTED,
                 () -> new BusinessException(ErrorCode.NOT_A_LEADER));

        MentoringParticipation mentoringParticipation = mentoringParticipationDataProvider.findParticipation(participantId);
        //검증
        mentoringParticipationPolicy.validateParticipationStatusForAcceptance(mentoringParticipation);
        //수락 처리
        mentoringParticipation.accept();
        redisApplicantManagementService.updateApplicantStatus(teamId,String.valueOf(mentoringParticipation.getUser().getId()),MentoringParticipationStatus.ACCEPTED);
    }

    /**
     * 지원 거절 로직
     * @param teamId
     * @param participant_id
     */
    @Transactional
    public void rejectMentoringParticipation(Long teamId, Long participantId) {
        User user = userDataProvider.getUser();
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(teamId);

        mentoringParticipationPolicy.validateParticipation(
                mentoringTeam, user, MentoringAuthority.LEADER, MentoringParticipationStatus.ACCEPTED,
                 () -> new BusinessException(ErrorCode.NOT_A_LEADER));

        MentoringParticipation rejectParticipation = mentoringParticipationDataProvider.findParticipation(participantId);

        mentoringParticipationPolicy.validateParticipationStatusForAcceptance(rejectParticipation);
        //거절처리
        redisApplicantManagementService.updateApplicantStatus(teamId,String.valueOf(rejectParticipation.getUser().getId()),MentoringParticipationStatus.REJECTED);
        // DB에서 지원자 데이터 삭제
        removeParticipant(rejectParticipation, rejectParticipation.getUser(), mentoringTeam);
    }

    /**
     * 강퇴하는 로직
     * @param teamId
     * @param userId
     */

    @Transactional
    public void exportTeamUser(Long teamId, Long userId) {
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
        redisParticipationManagementService.saveParticipation(mentoringTeam.getId(), exportUser.getId(), exportedParticipation);
        removeParticipant(export,exportUser,mentoringTeam);
    }

    /**
     * 탈퇴하는 로직
     * @param teamId
     */
    @Transactional
    public void deleteUser(Long teamId) {
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
        redisParticipationManagementService.saveParticipation(mentoringTeam.getId(), user.getId(),teamUserResponse);
        removeParticipant(teamUser,user,mentoringTeam);
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

        // 비동기로 팀원 가져오기
        CompletableFuture<List<TeamUserResponse>> teamUsersFuture  = asyncParticipationService.fetchAndSetTeamUsers(mentoringTeam, user.getId(), currentParticipation.getId());
        //비동기로 강퇴,탈퇴한 팀원 가져오기
        CompletableFuture<List<TeamUserResponse>> deletedOrExportedUsersFuture  = asyncParticipationService.fetchAndValidateDeletedOrExportedUsers(teamId, currentParticipation.getId());

        CompletableFuture<List<TeamParticipationResponse>> applicantsFuture =
                (currentParticipation.getAuthority() == MentoringAuthority.LEADER) ? asyncParticipationService.fetchApplicantsAsync(teamId)
                        : CompletableFuture.completedFuture(Collections.emptyList());

        CompletableFuture.allOf(teamUsersFuture, deletedOrExportedUsersFuture, applicantsFuture).join();

        try {
            // 모든 비동기 작업 병렬 실행 후 필요한 시점에서 결과 가져오기
            List<TeamUserResponse> teamUsers = teamUsersFuture.get();
            List<TeamUserResponse> deletedOrExportedUsers = deletedOrExportedUsersFuture.get();
            List<TeamUserResponse> combineUsers = TeamUserResponse.combine(teamUsers, deletedOrExportedUsers);

            // 권한별 응답 처리
            switch (currentParticipation.getAuthority()) {
                case LEADER:
                    List<TeamParticipationResponse> applicants = applicantsFuture.get();
                    return new ParticipantsResponse<>(mentoringTeam.getId(), MentoringAuthority.LEADER, new ForLeaderResponse(combineUsers, applicants));

                case CREW:
                    return new ParticipantsResponse<>(mentoringTeam.getId(), MentoringAuthority.CREW, combineUsers);

                default:
                    throw new BusinessException(ErrorCode.NOT_A_MEMBER);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new BusinessException(ErrorCode.ASYNC_OPERATION_FAILED);
        }
    }



    private void removeParticipant(MentoringParticipation mentoringParticipation,User user, MentoringTeam mentoringTeam) {
        mentoringParticipation.removeMentoringTeam(mentoringTeam);
        mentoringParticipation.removeUser(user);
        mentoringParticipationRepository.delete(mentoringParticipation);
    }

}
