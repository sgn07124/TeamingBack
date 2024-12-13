package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.dto.response.*;
import com.project.Teaming.domain.mentoring.entity.*;
import com.project.Teaming.domain.mentoring.repository.MentoringParticipationRepository;
import com.project.Teaming.domain.mentoring.repository.MentoringTeamRepository;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.MentoringParticipationAlreadyExistException;
import com.project.Teaming.global.error.exception.MentoringParticipationNotFoundException;
import com.project.Teaming.global.error.exception.MentoringTeamNotFoundException;
import com.project.Teaming.global.error.exception.NoAuthorityException;
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
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MentoringParticipationService {

    private final MentoringParticipationRepository mentoringParticipationRepository;
    private final MentoringTeamRepository mentoringTeamRepository;
    private final UserRepository userRepository;

    /**
     * 지원자로 등록하는 로직
     * @param mentoringTeamId
     * @param role
     */
    @Transactional
    public Long saveMentoringParticipation(Long mentoringTeamId, MentoringRole role) {
        User user = getUser();
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(mentoringTeamId).orElseThrow(MentoringTeamNotFoundException::new);
        Optional<MentoringParticipation> participation = mentoringParticipationRepository.findByMentoringTeamAndUser(mentoringTeam, user);
        if (participation.isPresent()) {
            if (participation.get().getParticipationStatus() == MentoringParticipationStatus.ACCEPTED) {
                throw new MentoringParticipationAlreadyExistException(ErrorCode.ALREADY_MEMBER_OF_TEAM);
            } else if (participation.get().getParticipationStatus() == MentoringParticipationStatus.EXPORT) {
                throw new NoAuthorityException(ErrorCode.EXPORTED_BY_TEAM);
            } else {
                throw new MentoringParticipationAlreadyExistException(ErrorCode.ALREADY_PARTICIPATED);
            }
        } else {
            MentoringParticipation mentoringParticipation = MentoringParticipation.builder()
                    .participationStatus(MentoringParticipationStatus.PENDING)
                    .authority(MentoringAuthority.NoAuth)
                    .role(role)
                    .requestDate(LocalDateTime.now())
                    .reportingCnt(0)
                    .isDeleted(false)
                    .build();
            mentoringParticipation.setUser(user);
            mentoringParticipation.addMentoringTeam(mentoringTeam);
            MentoringParticipation saved = mentoringParticipationRepository.save(mentoringParticipation);
            return saved.getId();
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
        Optional<MentoringParticipation> participation = mentoringParticipationRepository.findByMentoringTeamAndUser(mentoringTeam, user);
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
        Optional<MentoringParticipation> teamLeader = mentoringParticipationRepository.findByMentoringTeamAndUserAndAuthority(mentoringTeam, user, MentoringAuthority.LEADER);
        if (teamLeader.isEmpty()) {
            throw new NoAuthorityException(ErrorCode.NOT_A_LEADER);
        }
        MentoringParticipation mentoringParticipation = mentoringParticipationRepository.findById(participant_id).orElseThrow(MentoringParticipationNotFoundException::new);
        if ( mentoringParticipation.getAuthority() == MentoringAuthority.NoAuth && mentoringParticipation.getParticipationStatus() == MentoringParticipationStatus.PENDING) {
            mentoringParticipation.setParticipationStatus(MentoringParticipationStatus.ACCEPTED);
            mentoringParticipation.setAuthority(MentoringAuthority.CREW);
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
        Optional<MentoringParticipation> teamLeader = mentoringParticipationRepository.findByMentoringTeamAndUserAndAuthority(mentoringTeam, user, MentoringAuthority.LEADER);
        if (teamLeader.isEmpty()) {
            throw new NoAuthorityException(ErrorCode.NOT_A_LEADER);
        }
        MentoringParticipation mentoringParticipation = mentoringParticipationRepository.findById(participant_id).orElseThrow(MentoringParticipationNotFoundException::new);
        if (mentoringParticipation.getAuthority() == MentoringAuthority.NoAuth && mentoringParticipation.getParticipationStatus() == MentoringParticipationStatus.PENDING) {
            mentoringParticipation.setParticipationStatus(MentoringParticipationStatus.REJECTED);
            mentoringParticipation.setDecisionDate(LocalDateTime.now());
        } else {
            throw new NoAuthorityException(ErrorCode.STATUS_IS_NOT_PENDING);
        }
    }

    /**
     * 탈퇴하는 로직
     * @param teamId
     */
    @Transactional
    public void deleteUser(Long teamId) {
        User user = getUser();
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(teamId).orElseThrow(MentoringTeamNotFoundException::new);
        Optional<MentoringParticipation> teamUser = mentoringParticipationRepository.findByMentoringTeamAndUser(mentoringTeam, user);
        if (teamUser.isPresent() && !teamUser.get().getIsDeleted()) {
            teamUser.get().setDeleted(true);
        } else {
            throw new NoAuthorityException(ErrorCode.NOT_A_MEMBER);
        }
    }

    public ParticipantsDto<?> getParticipantsInfo(Long teamId) {
        User user = getUser();
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(teamId).orElseThrow(MentoringTeamNotFoundException::new);
        Optional<MentoringParticipation> teamUser = mentoringParticipationRepository.findByMentoringTeamAndUser(mentoringTeam,user);
        if (teamUser.isPresent()) {  //팀과의 연관관계가 있으면
            if (teamUser.get().getAuthority() == MentoringAuthority.LEADER && !teamUser.get().getIsDeleted()) {  //팀의 리더인 유저
                List<RsTeamUserDto> allTeamUsers =  mentoringParticipationRepository.findAllByMemberStatus(mentoringTeam, MentoringParticipationStatus.ACCEPTED,MentoringParticipationStatus.EXPORT);
                List<RsTeamParticipationDto> participations = mentoringParticipationRepository.findAllForLeader(teamId,MentoringAuthority.LEADER,MentoringParticipationStatus.EXPORT);
                LeaderResponseDto dto = new LeaderResponseDto();
                dto.setMembers(allTeamUsers);
                dto.setParticipations(participations);
                List<Object> responseList = new ArrayList<>();
                responseList.add(MentoringAuthority.LEADER);
                responseList.add(dto);
                return new ParticipantsDto<>(responseList);
            } else if (teamUser.get().getAuthority() == MentoringAuthority.CREW && !teamUser.get().getIsDeleted()) {  //팀의 멤버인 유저
                List<RsTeamUserDto> members = mentoringParticipationRepository.findAllByMemberStatus(mentoringTeam, MentoringParticipationStatus.ACCEPTED,MentoringParticipationStatus.EXPORT);
                List<Object> responseList = new ArrayList<>();
                responseList.add(MentoringAuthority.CREW);
                responseList.add(members);
                return new ParticipantsDto<>(responseList);
            } else {  //지원만 한 유저 , 수정필요
                List<RsUserParticipationDto> forUser = mentoringParticipationRepository.findAllForUser(teamId, MentoringAuthority.LEADER, MentoringParticipationStatus.EXPORT);
                List<Object> responseList = new ArrayList<>();
                responseList.add(MentoringAuthority.NoAuth);
                responseList.add(forUser);
                return new ParticipantsDto<>(responseList);
            }
        } else { //팀과 무관한 사용자
            List<RsUserParticipationDto> forUser = mentoringParticipationRepository.findAllForUser(teamId, MentoringAuthority.LEADER, MentoringParticipationStatus.EXPORT);
            List<Object> responseList = new ArrayList<>();
            responseList.add(MentoringAuthority.NoAuth);
            responseList.add(forUser);
            return new ParticipantsDto<>(responseList);
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
