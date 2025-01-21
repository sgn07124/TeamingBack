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
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentoringParticipationService {

    private final CacheManager cacheManager;
    private final MentoringParticipationRepository mentoringParticipationRepository;
    private final MentoringParticipationDataProvider mentoringParticipationDataProvider;
    private final MentoringParticipationPolicy mentoringParticipationPolicy;
    private final MentoringTeamDataProvider mentoringTeamDataProvider;
    private final UserDataProvider userDataProvider;
    private final RedisParticipationManagementService redisParticipationManagementService;
    private final TeamParticipationCacheService cacheService;
    private final ReviewService reviewService;
    private final ReportService reportService;

    /**
     * 지원자로 등록하는 로직
     * @param dto
     * @return
     */
    @Transactional
    public MentoringParticipation saveMentoringParticipation(Long teamId, ParticipationRequest dto) {
        User user = userDataProvider.getUser();
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(teamId);
        Optional<MentoringParticipation> participation = mentoringParticipationRepository.findDynamicMentoringParticipation(
                mentoringTeam, user,null,null,null);

        participation.ifPresent(mentoringParticipationPolicy::validateParticipationStatus);
        MentoringParticipation mentoringParticipation = MentoringParticipation.from(dto);

        if (dto.getAuthority() == MentoringAuthority.LEADER) {
            mentoringParticipation.setDecisionDate(LocalDateTime.now());
        }
        mentoringParticipation.setUser(user);
        mentoringParticipation.addMentoringTeam(mentoringTeam);
        MentoringParticipation savedParticipation = mentoringParticipationRepository.save(mentoringParticipation);

        return savedParticipation;
    }

    /**
     * 지원 취소하는 로직
     * @param mentoringTeamId
     */
    @Transactional
    public TeamParticipationResponse cancelMentoringParticipation(Long teamId) {
        User user = userDataProvider.getUser();
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(teamId);

        MentoringParticipation participation = mentoringParticipationDataProvider.findParticipationWith(
                mentoringTeam, user,null,null,null,() -> new BusinessException(ErrorCode.MENTORING_PARTICIPATION_NOT_EXIST));

        mentoringParticipationPolicy.validateCancellation(participation);
        TeamParticipationResponse participationDto = TeamParticipationResponse.toParticipationDto(participation);
        removeParticipant(participation,user,mentoringTeam);
        return participationDto;
    }

    /**
     * 지원 수락 로직
     * @param team_Id
     * @param participant_id
     */
    @Transactional
    public TeamParticipationResponse acceptMentoringParticipation(Long teamId, Long participantId) {
        User user = userDataProvider.getUser();
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(teamId);

        mentoringParticipationPolicy.validateParticipation(
                mentoringTeam, user, MentoringAuthority.LEADER ,MentoringParticipationStatus.ACCEPTED,
                null, () -> new BusinessException(ErrorCode.NOT_A_LEADER));

        MentoringParticipation mentoringParticipation = mentoringParticipationDataProvider.findParticipation(participantId);
        //검증
        mentoringParticipationPolicy.validateParticipationStatusForAcceptance(mentoringParticipation);
        //수락 처리
        mentoringParticipation.accept();

        return TeamParticipationResponse.toParticipationDto(mentoringParticipation);
    }

    /**
     * 지원 거절 로직
     * @param teamId
     * @param participant_id
     */
    @Transactional
    public TeamParticipationResponse rejectMentoringParticipation(Long teamId, Long participantId) {
        User user = userDataProvider.getUser();
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(teamId);

        mentoringParticipationPolicy.validateParticipation(
                mentoringTeam, user, MentoringAuthority.LEADER, MentoringParticipationStatus.ACCEPTED,
                null, () -> new BusinessException(ErrorCode.NOT_A_LEADER));

        MentoringParticipation rejectParticipation = mentoringParticipationDataProvider.findParticipation(participantId);

        mentoringParticipationPolicy.validateParticipationStatusForAcceptance(rejectParticipation);
        //거절처리
        rejectParticipation.reject();
        TeamParticipationResponse reject = TeamParticipationResponse.toParticipationDto(rejectParticipation);
        // DB에서 지원자 데이터 삭제
        removeParticipant(rejectParticipation, rejectParticipation.getUser(), mentoringTeam);
        return reject;
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
                null, () -> new BusinessException(ErrorCode.NOT_A_LEADER));

        MentoringParticipation export = mentoringParticipationDataProvider.findParticipationWith(
                mentoringTeam, exportUser,MentoringAuthority.CREW, MentoringParticipationStatus.ACCEPTED,
                null,() -> new BusinessException(ErrorCode.EXPORTED_MEMBER_NOT_EXISTS));
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
                null, () -> new BusinessException(ErrorCode.NOT_A_MEMBER));

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
                null, () -> new BusinessException(ErrorCode.NOT_A_MEMBER));

        // 리뷰여부 검증 포함
        List<TeamUserResponse> teamUsers = mentoringParticipationRepository.findAllByMemberStatus(mentoringTeam, MentoringStatus.COMPLETE,
                MentoringParticipationStatus.ACCEPTED, currentParticipation.getId());
        setLoginStatus(teamUsers,user.getId());

        List<TeamUserResponse> deletedOrExportedTeamUsers = redisParticipationManagementService.getDeletedOrExportedParticipations(teamId);

        // 리스트가 비어 있지 않은 경우에만 검증 수행
        if (deletedOrExportedTeamUsers != null && !deletedOrExportedTeamUsers.isEmpty()) {
            // 로그인 한 사용자의 리뷰여부 검증
            reviewService.setReviewInfo(deletedOrExportedTeamUsers, currentParticipation.getId());
            // 로그인 한 사용자의 신고여부 검증
            reportService.setReportInfo(deletedOrExportedTeamUsers, currentParticipation.getId());
        }

        //decisionDate기준 정렬
        List<TeamUserResponse> combineUsers = TeamUserResponse.combine(teamUsers, deletedOrExportedTeamUsers);

        //권한에 따라 dto 반환
        if (currentParticipation.getAuthority() == MentoringAuthority.LEADER) {  //팀의 리더인 유저
            log.info("All Team Users For Leader: {}", combineUsers);
            // 지원자 현황 캐싱된 데이터 조회
            Map<String, TeamParticipationResponse> cachedParticipations = cacheService.get(teamId);
            // 캐시 데이터에서 지원자 리스트 추출
            List<TeamParticipationResponse> participations = cachedParticipations != null
                    ? new ArrayList<>(cachedParticipations.values())
                    : new ArrayList<>();

            ForLeaderResponse dto = new ForLeaderResponse();
            dto.setMembers(combineUsers);
            dto.setParticipations(participations);
            log.info("Participations for Leader: {}", participations);
            return new ParticipantsResponse<>(mentoringTeam.getId(),MentoringAuthority.LEADER, dto);

        } else if (currentParticipation.getAuthority() == MentoringAuthority.CREW) {
            //팀 유저만 반환
            log.info("Members for Crew: {}", combineUsers);
            return new ParticipantsResponse<>(mentoringTeam.getId(),MentoringAuthority.CREW, combineUsers);
        }
        else throw new BusinessException(ErrorCode.NOT_A_MEMBER);

    }

    /**
     * 로그인 한 사용자 있는지 확인하는 로직
     * @param dtos
     * @param userId
     */
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

}
