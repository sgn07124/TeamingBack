package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.dto.response.RsTeamParticipationDto;
import com.project.Teaming.domain.mentoring.dto.response.RsTeamUserDto;
import com.project.Teaming.domain.mentoring.dto.response.RsUserParticipationDto;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
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
     * @param userId
     * @param mentoringTeamId
     * @param role
     */
    @Transactional
    public Long saveMentoringParticipation(Long userId, Long mentoringTeamId, MentoringRole role) {
        User findUser = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(mentoringTeamId).orElseThrow(MentoringTeamNotFoundException::new);
        Optional<MentoringParticipation> participation = mentoringParticipationRepository.findByMentoringTeamAndUser(mentoringTeam, findUser);
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
            mentoringParticipation.setUser(findUser);
            mentoringParticipation.addMentoringTeam(mentoringTeam);
            MentoringParticipation saved = mentoringParticipationRepository.save(mentoringParticipation);
            return saved.getId();
        }
    }

    /**
     * 지원 취소하는 로직
     * @param userId
     * @param mentoringTeamId
     */
    @Transactional
    public void cancelMentoringParticipation(Long userId, Long mentoringTeamId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));
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
     * @param userId
     * @param team_Id
     * @param participant_id
     */
    @Transactional
    public void acceptMentoringParticipation(Long userId, Long team_Id, Long participant_id) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(team_Id).orElseThrow(MentoringTeamNotFoundException::new);
        Optional<MentoringParticipation> teamLeader = mentoringParticipationRepository.existsByMentoringTeamAndUserAndAuthority(mentoringTeam, user, MentoringAuthority.LEADER);
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
     * @param userId
     * @param teamId
     * @param participant_id
     */
    @Transactional
    public void rejectMentoringParticipation(Long userId, Long teamId, Long participant_id) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(teamId).orElseThrow(MentoringTeamNotFoundException::new);
        Optional<MentoringParticipation> teamLeader = mentoringParticipationRepository.existsByMentoringTeamAndUserAndAuthority(mentoringTeam, user, MentoringAuthority.LEADER);
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
     * @param userId
     * @param teamId
     */
    @Transactional
    public void deleteUser(Long userId, Long teamId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(teamId).orElseThrow(MentoringTeamNotFoundException::new);
        Optional<MentoringParticipation> teamUser = mentoringParticipationRepository.findByMentoringTeamAndUser(mentoringTeam, user);
        if (teamUser.isPresent() && !teamUser.get().getIsDeleted()) {
            teamUser.get().setDeleted(true);
        } else {
            throw new NoAuthorityException(ErrorCode.NOT_A_MEMBER);
        }
    }


    /**
     * 리더 페이지용
     * 지원자 현황 조회용 로직
     * @param teamId
     * @return
     */
    public List<RsTeamParticipationDto> findForLeader(Long teamId) {
        return mentoringParticipationRepository.findAllForLeader(teamId,MentoringAuthority.LEADER,MentoringParticipationStatus.EXPORT);
    }

    /**
     * 일반 사용자 페이지용
     * 지원자 현황 조회용 로직
     * 팀원이 되었다가 내보내진 유저는 보여지지 않도록
     * @param teamId
     * @return
     */
    public List<RsUserParticipationDto> findForUser(Long teamId) {
        List<RsUserParticipationDto> result = mentoringParticipationRepository.findAllForUser(teamId, MentoringAuthority.LEADER, MentoringParticipationStatus.EXPORT);
        return (result.isEmpty()) ? Collections.emptyList() : result;
    }

    /**
     * 팀원 조회용 로직
     * @param team
     * @return
     */
    public List<RsTeamUserDto> findAllTeamUsers(MentoringTeam team) {
        return mentoringParticipationRepository.findAllByMemberStatus(team, MentoringParticipationStatus.ACCEPTED,MentoringParticipationStatus.EXPORT);
    }

    public Optional<MentoringParticipation> findBy(MentoringTeam mentoringTeam, User user) {
        return mentoringParticipationRepository.findByMentoringTeamAndUserAndParticipationStatus(mentoringTeam,user,MentoringParticipationStatus.ACCEPTED);
    }
    public Optional<MentoringParticipation> findByTeamAndUser(MentoringTeam mentoringTeam, User user) {
        return mentoringParticipationRepository.findByMentoringTeamAndUser(mentoringTeam,user);
    }

}
