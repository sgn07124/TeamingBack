package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.dto.request.ParticipationRequest;
import com.project.Teaming.domain.mentoring.dto.response.*;
import com.project.Teaming.domain.mentoring.entity.*;
import com.project.Teaming.domain.mentoring.provider.MentoringParticipationDataProvider;
import com.project.Teaming.domain.mentoring.provider.MentoringTeamDataProvider;
import com.project.Teaming.domain.mentoring.provider.UserDataProvider;
import com.project.Teaming.domain.mentoring.repository.MentoringParticipationRepository;
import com.project.Teaming.domain.mentoring.repository.MentoringTeamRepository;
import com.project.Teaming.domain.mentoring.service.policy.MentoringParticipationPolicy;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.*;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentoringParticipationService {

    private final MentoringParticipationRepository mentoringParticipationRepository;
    private final MentoringParticipationDataProvider mentoringParticipationDataProvider;
    private final MentoringParticipationPolicy mentoringParticipationPolicy;
    private final MentoringTeamDataProvider mentoringTeamDataProvider;
    private final UserDataProvider userDataProvider;
    private final RedisParticipationManagementService redisParticipationManagementService;

    /**
     * 지원자로 등록하는 로직
     * @param dto
     * @return
     */
    @Transactional
    public MentoringParticipation saveMentoringParticipation(MentoringTeam mentoringTeam, ParticipationRequest dto) {
        User user = userDataProvider.getUser();
        Optional<MentoringParticipation> participation = mentoringParticipationRepository.findDynamicMentoringParticipation(
                mentoringTeam, user,null,null,null);

        participation.ifPresent(mentoringParticipationPolicy::validateParticipationStatus);

        MentoringParticipation mentoringParticipation = MentoringParticipation.from(dto);
        mentoringParticipation.setUser(user);
        mentoringParticipation.addMentoringTeam(mentoringTeam);
        return mentoringParticipationRepository.save(mentoringParticipation);

    }

    /**
     * 지원 취소하는 로직
     * @param mentoringTeamId
     */
    @Transactional
    public void cancelMentoringParticipation(Long mentoringTeamId) {
        User user = userDataProvider.getUser();
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(mentoringTeamId);
        MentoringParticipation participation = mentoringParticipationDataProvider.findParticipationWith(
                mentoringTeam, user,null,null,null,() -> new BusinessException(ErrorCode.MENTORING_PARTICIPATION_NOT_EXIST));

        mentoringParticipationPolicy.validateCancellation(participation);

        participation.removeMentoringTeam(mentoringTeam);  // 연관 관계 해제
        participation.removeUser(user);                    // 연관 관계 해제
        mentoringParticipationRepository.delete(participation);
    }

    /**
     * 지원 수락 로직
     * @param team_Id
     * @param participant_id
     */
    @Transactional
    public void acceptMentoringParticipation(Long team_Id, Long participant_id) {
        User user = userDataProvider.getUser();
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(team_Id);

        mentoringParticipationPolicy.validateParticipation(
                mentoringTeam, user, MentoringAuthority.LEADER ,MentoringParticipationStatus.ACCEPTED,
                null,false, () -> new BusinessException(ErrorCode.NOT_A_LEADER));

        MentoringParticipation mentoringParticipation = mentoringParticipationDataProvider.findParticipation(participant_id);

        mentoringParticipationPolicy.validateParticipationStatusForAcceptance(mentoringParticipation);

        mentoringParticipation.accept();
        mentoringParticipation.setCrew();
        mentoringParticipation.setDecisionDate(LocalDateTime.now());
    }

    /**
     * 지원 거절 로직
     * @param teamId
     * @param participant_id
     */
    @Transactional
    public void rejectMentoringParticipation(Long teamId, Long participant_id) {
        User user = userDataProvider.getUser();
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(teamId);

        mentoringParticipationPolicy.validateParticipation(
                mentoringTeam, user, MentoringAuthority.LEADER, MentoringParticipationStatus.ACCEPTED,
                null,false, () -> new BusinessException(ErrorCode.NOT_A_LEADER));

        MentoringParticipation mentoringParticipation = mentoringParticipationDataProvider.findParticipation(participant_id);

        mentoringParticipationPolicy.validateParticipationStatusForAcceptance(mentoringParticipation);

        mentoringParticipation.reject();
        mentoringParticipation.setDecisionDate(LocalDateTime.now());

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
                null,false, () -> new BusinessException(ErrorCode.NOT_A_LEADER));

        MentoringParticipation export = mentoringParticipationDataProvider.findParticipationWith(
                mentoringTeam, exportUser,MentoringAuthority.CREW, MentoringParticipationStatus.ACCEPTED,
                null,() -> new BusinessException(ErrorCode.EXPORTED_MEMBER_NOT_EXISTS));
        // 강퇴
        export.export();
        redisParticipationManagementService.saveParticipation(export, exportUser);
        export.removeUser(exportUser);
        export.removeMentoringTeam(mentoringTeam);
        mentoringParticipationRepository.delete(export);
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
        teamUser.setDeleted(true);
        redisParticipationManagementService.saveParticipation(teamUser, user);
        teamUser.removeMentoringTeam(mentoringTeam);
        teamUser.removeUser(user);
        mentoringParticipationRepository.delete(teamUser);
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

        MentoringParticipation currentUser = mentoringParticipationDataProvider.findParticipationWith(
                mentoringTeam, user, null, MentoringParticipationStatus.ACCEPTED,
                null, () -> new BusinessException(ErrorCode.NOT_A_MEMBER));

        List<TeamUserResponse> teamUsers = mentoringParticipationRepository.findAllByMemberStatus(mentoringTeam, MentoringStatus.COMPLETE,
                MentoringParticipationStatus.ACCEPTED, currentUser.getId());
        setLoginStatus(teamUsers,user.getId());
        List<TeamUserResponse> deletedOrExportedTeamUsers = redisParticipationManagementService.getDeletedOrExportedParticipations(teamId);
        List<TeamUserResponse> combineUsers = TeamUserResponse.combine(teamUsers, deletedOrExportedTeamUsers);

        if (currentUser.getAuthority() == MentoringAuthority.LEADER) {  //팀의 리더인 유저
            log.info("All Team Users For Leader: {}", combineUsers);
            //지원자현황조회
            List<TeamParticipationResponse> participations = mentoringParticipationRepository.findAllForLeader(teamId, MentoringAuthority.LEADER);

            ForLeaderResponse dto = new ForLeaderResponse();
            dto.setMembers(combineUsers);
            dto.setParticipations(participations);
            log.info("Participations for Leader: {}", participations);
            return new ParticipantsResponse<>(mentoringTeam.getId(),MentoringAuthority.LEADER, dto);

        } else if (currentUser.getAuthority() == MentoringAuthority.CREW) {
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

}
