package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.dto.request.ParticipationRequest;
import com.project.Teaming.domain.mentoring.dto.response.*;
import com.project.Teaming.domain.mentoring.entity.*;
import com.project.Teaming.domain.mentoring.repository.MentoringParticipationRepository;
import com.project.Teaming.domain.mentoring.repository.MentoringTeamRepository;
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
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentoringParticipationService {

    private final MentoringParticipationRepository mentoringParticipationRepository;
    private final MentoringTeamRepository mentoringTeamRepository;
    private final UserRepository userRepository;

    /**
     * 지원자로 등록하는 로직
     * @param dto
     * @return
     */
    @Transactional
    public MentoringParticipation saveMentoringParticipation(MentoringTeam mentoringTeam, ParticipationRequest dto) {
        User user = getUser();
        Optional<MentoringParticipation> participation = mentoringParticipationRepository.findDynamicMentoringParticipation(
                mentoringTeam, user,null,null,null,false);
        if (participation.isPresent()) {
            if (participation.get().getParticipationStatus() == MentoringParticipationStatus.ACCEPTED) {
                throw new MentoringParticipationAlreadyExistException(ErrorCode.ALREADY_MEMBER_OF_TEAM);
            } else if (participation.get().getParticipationStatus() == MentoringParticipationStatus.EXPORT) {
                throw new NoAuthorityException(ErrorCode.EXPORTED_BY_TEAM);
            } else {
                throw new MentoringParticipationAlreadyExistException(ErrorCode.ALREADY_PARTICIPATED);
            }
        } else {
            MentoringParticipation mentoringParticipation = MentoringParticipation.from(dto);
            mentoringParticipation.setUser(user);
            mentoringParticipation.addMentoringTeam(mentoringTeam);
            MentoringParticipation saved = mentoringParticipationRepository.save(mentoringParticipation);
            return saved;
        }
    }

    /**
     * 지원 취소하는 로직
     * @param mentoringTeamId
     */
    @Transactional
    public void cancelMentoringParticipation(Long mentoringTeamId) {
        User user = getUser();
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(mentoringTeamId).orElseThrow(MentoringTeamNotFoundException::new);
        Optional<MentoringParticipation> participation = mentoringParticipationRepository.findDynamicMentoringParticipation(
                mentoringTeam, user,null,null,null,false);
        if (participation.isPresent()) {
            if (participation.get().getParticipationStatus() == MentoringParticipationStatus.PENDING) {
                participation.get().removeMentoringTeam(mentoringTeam);  //연관관계 해제
                participation.get().removeUser(user);  //연관관계 해제
                mentoringParticipationRepository.delete(participation.get());
            } else if (participation.get().getParticipationStatus() == MentoringParticipationStatus.ACCEPTED) {
                throw new NoAuthorityException(ErrorCode.ALREADY_MEMBER_OF_TEAM);
            } else if (participation.get().getParticipationStatus() == MentoringParticipationStatus.EXPORT) {
                throw new NoAuthorityException(ErrorCode.EXPORTED_BY_TEAM);
            } else throw new NoAuthorityException(ErrorCode.REJECTED_FROM_MENTORING_TEAM);
        } else {
            throw new MentoringParticipationNotFoundException(ErrorCode.MENTORING_PARTICIPATION_NOT_EXIST);
        }
    }

    /**
     * 지원 수락 로직
     * @param team_Id
     * @param participant_id
     */
    @Transactional
    public void acceptMentoringParticipation(Long team_Id, Long participant_id) {
        User user = getUser();
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(team_Id).orElseThrow(MentoringTeamNotFoundException::new);
        Optional<MentoringParticipation> teamLeader = mentoringParticipationRepository.findDynamicMentoringParticipation(
                mentoringTeam, user, MentoringAuthority.LEADER ,MentoringParticipationStatus.ACCEPTED,null,false);
        if (teamLeader.isEmpty()) {
            throw new NoAuthorityException(ErrorCode.NOT_A_LEADER);
        }
        MentoringParticipation mentoringParticipation = mentoringParticipationRepository.findById(participant_id).orElseThrow(MentoringParticipationNotFoundException::new);
        if ( mentoringParticipation.getAuthority() == MentoringAuthority.NoAuth && mentoringParticipation.getParticipationStatus() == MentoringParticipationStatus.PENDING) {
            mentoringParticipation.accept();
            mentoringParticipation.setCrew();
            mentoringParticipation.setDecisionDate(LocalDateTime.now());
        } else {
            throw new NoAuthorityException(ErrorCode.STATUS_IS_NOT_PENDING);
        }
    }

    /**
     * 지원 거절 로직
     * @param teamId
     * @param participant_id
     */
    @Transactional
    public void rejectMentoringParticipation(Long teamId, Long participant_id) {
        User user = getUser();
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(teamId).orElseThrow(MentoringTeamNotFoundException::new);
        Optional<MentoringParticipation> teamLeader = mentoringParticipationRepository.findDynamicMentoringParticipation(
                mentoringTeam, user, MentoringAuthority.LEADER, MentoringParticipationStatus.ACCEPTED,null,false);
        if (teamLeader.isEmpty()) {
            throw new NoAuthorityException(ErrorCode.NOT_A_LEADER);
        }
        MentoringParticipation mentoringParticipation = mentoringParticipationRepository.findById(participant_id).orElseThrow(MentoringParticipationNotFoundException::new);
        if (mentoringParticipation.getAuthority() == MentoringAuthority.NoAuth && mentoringParticipation.getParticipationStatus() == MentoringParticipationStatus.PENDING) {
            mentoringParticipation.reject();
            mentoringParticipation.setDecisionDate(LocalDateTime.now());
        } else {
            throw new NoAuthorityException(ErrorCode.STATUS_IS_NOT_PENDING);
        }
    }

    /**
     * 강퇴하는 로직
     * @param teamId
     * @param userId
     */

    @Transactional
    public void exportTeamUser(Long teamId, Long userId) {
        User user = getUser();
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(teamId).orElseThrow(MentoringTeamNotFoundException::new);
        User exportUser = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        Optional<MentoringParticipation> teamLeader = mentoringParticipationRepository.findDynamicMentoringParticipation(
                mentoringTeam, user, MentoringAuthority.LEADER,MentoringParticipationStatus.ACCEPTED,null,false);
        if (teamLeader.isEmpty()) {
            throw new NoAuthorityException(ErrorCode.NOT_A_LEADER);
        }
        Optional<MentoringParticipation> export = mentoringParticipationRepository.findDynamicMentoringParticipation(
                mentoringTeam, exportUser,MentoringAuthority.CREW, MentoringParticipationStatus.ACCEPTED,null,false);
        if (export.isPresent()) {
            export.get().export();
        }
        else throw new BusinessException(ErrorCode.EXPORTED_MEMBER_NOT_EXISTS);
    }

    /**
     * 탈퇴하는 로직
     * @param teamId
     */
    @Transactional
    public void deleteUser(Long teamId) {
        User user = getUser();
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(teamId).orElseThrow(MentoringTeamNotFoundException::new);
        Optional<MentoringParticipation> teamUser = mentoringParticipationRepository.findDynamicMentoringParticipation(
                mentoringTeam, user,null,MentoringParticipationStatus.ACCEPTED,null,false);
        if (teamUser.isPresent()) {
            if (teamUser.get().getAuthority() == MentoringAuthority.LEADER) {
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
            teamUser.get().setDeleted(true);
        } else {
            throw new NoAuthorityException(ErrorCode.NOT_A_MEMBER);
        }
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
        User user = getUser();
        log.info("Logged-in User: {}", user);
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(teamId).orElseThrow(MentoringTeamNotFoundException::new);
        log.info("Found MentoringTeam: {}", mentoringTeam);
        Optional<MentoringParticipation> currentTeamUser = mentoringParticipationRepository.findDynamicMentoringParticipation(
                mentoringTeam, user, null, MentoringParticipationStatus.ACCEPTED, null, false);
        log.info("Current Team User: {}", currentTeamUser);

        if (currentTeamUser.isPresent()) {  //팀과의 연관관계가 있으면
            MentoringParticipation teamUser = currentTeamUser.get();
            log.info("Team User Details: {}", teamUser);
            if (teamUser.getAuthority() == MentoringAuthority.LEADER) {  //팀의 리더인 유저
                List<TeamUserResponse> allTeamUsers = mentoringParticipationRepository.findAllByMemberStatus(mentoringTeam, MentoringStatus.COMPLETE,
                        MentoringParticipationStatus.ACCEPTED, MentoringParticipationStatus.EXPORT, teamUser.getId());
                setLoginStatus(allTeamUsers,user.getId());
                log.info("All Team Users For Leader: {}", allTeamUsers);
                List<TeamParticipationResponse> participations = mentoringParticipationRepository.findAllForLeader(teamId, MentoringAuthority.LEADER);
                ForLeaderResponse dto = new ForLeaderResponse();
                dto.setMembers(allTeamUsers);
                dto.setParticipations(participations);
                log.info("Participations for Leader: {}", participations);
                return new ParticipantsResponse<>(MentoringAuthority.LEADER, dto);

            } else if (teamUser.getAuthority() == MentoringAuthority.CREW) {  //팀의 멤버인 유저
                List<TeamUserResponse> members = mentoringParticipationRepository.findAllByMemberStatus(mentoringTeam, MentoringStatus.COMPLETE,
                        MentoringParticipationStatus.ACCEPTED, MentoringParticipationStatus.EXPORT, teamUser.getId());
                setLoginStatus(members,user.getId());
                log.info("Members for Crew: {}", members);
                return new ParticipantsResponse<>(MentoringAuthority.CREW, members);
            }
            else throw new BusinessException(ErrorCode.NOT_A_MEMBER);
        }
        else throw new BusinessException(ErrorCode.NOT_A_MEMBER);
    }


    private User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        Long userId = securityUser.getUserId();
        return userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
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
