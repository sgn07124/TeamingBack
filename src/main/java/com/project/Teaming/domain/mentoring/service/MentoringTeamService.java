package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.dto.request.ParticipationRequest;
import com.project.Teaming.domain.mentoring.dto.request.TeamRequest;
import com.project.Teaming.domain.mentoring.dto.response.*;
import com.project.Teaming.domain.mentoring.entity.*;
import com.project.Teaming.domain.mentoring.repository.*;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.error.exception.MentoringTeamNotFoundException;
import com.project.Teaming.global.error.exception.NoAuthorityException;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class MentoringTeamService {

    @PersistenceContext
    private final EntityManager entityManager;

    private final MentoringTeamRepository mentoringTeamRepository;
    private final UserRepository userRepository;
    private final TeamCategoryService teamCategoryService;
    private final MentoringParticipationService mentoringParticipationService;
    private final MentoringParticipationRepository mentoringParticipationRepository;
    private final MentoringBoardRepository mentoringBoardRepository;

    /**
     * 멘토링팀 생성, 저장 로직
     * @param dto
     */

    @Transactional
    public Long saveMentoringTeam(TeamRequest dto) {

        MentoringTeam mentoringTeam = MentoringTeam.from(dto);

        MentoringTeam saved = mentoringTeamRepository.save(mentoringTeam);
        //리더 생성
        ParticipationRequest participationDto = new ParticipationRequest(MentoringAuthority.LEADER, MentoringParticipationStatus.ACCEPTED, dto.getRole());
        MentoringParticipation leader = mentoringParticipationService.saveMentoringParticipation(mentoringTeam, participationDto);
        leader.setDecisionDate(LocalDateTime.now());

        //카테고리 생성
        teamCategoryService.saveTeamCategories(saved,dto.getCategories());
        return saved.getId();
    }

    /**
     * 멘토링 팀 수정 로직, 팀 구성원이며 팀장이여야 가능하다
     * @param mentoringTeamId
     * @param dto
     */
    @Transactional
    public void updateMentoringTeam(Long mentoringTeamId, TeamRequest dto) {
        Long userId = getUser();
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));

        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(mentoringTeamId).orElseThrow(MentoringTeamNotFoundException::new);
        Optional<MentoringParticipation> teamLeader = mentoringParticipationRepository.findDynamicMentoringParticipation(
                mentoringTeam, user, MentoringAuthority.LEADER,MentoringParticipationStatus.ACCEPTED,null,false);
        if (mentoringTeam.getFlag() == Status.TRUE) {
            throw new NoAuthorityException("이미 삭제된 팀 입니다.");
        }
        if (teamLeader.isPresent()) {
            mentoringTeam.mentoringTeamUpdate(dto); //업데이트 메서드
            // 기존 카테고리 제거
            teamCategoryService.removeTeamCategories(mentoringTeam);
            // 새로운 카테고리 매핑
            teamCategoryService.saveTeamCategories(mentoringTeam, dto.getCategories());
        }
        else throw new NoAuthorityException("수정 할 권한이 없습니다");
    }


    /**
     * 특정 멘토링 팀을 찾는 로직
     * @param mentoringTeamId
     * @return
     */
    @Transactional(readOnly = true)
    public MentoringTeam findMentoringTeam(Long mentoringTeamId) {
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(mentoringTeamId).orElseThrow(MentoringTeamNotFoundException::new);
        if (mentoringTeam.getFlag() == Status.TRUE) {
            throw new MentoringTeamNotFoundException();
        }
        else return mentoringTeam;
    }

    /**
     * 내 멘토링 팀들을 모두 찾는 로직
     * @return
     */
    @Transactional(readOnly = true)
    public List<MentoringTeam> findMyMentoringTeams(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        return mentoringTeamRepository.findTeamsWithStatusAndUser(user,MentoringParticipationStatus.ACCEPTED);
    }

    @Transactional(readOnly = true)
    public List<MentoringTeam> getAuthenticateTeams() {
        Long userId = getUser();
        return findMyMentoringTeams(userId);
    }



    /**
     * 멘토링 팀 삭제 로직, 팀 구성원이고 리더여야 가능하다
     * @param mentoringTeamId
     */
    @Transactional
    public void deleteMentoringTeam(Long mentoringTeamId) {
        Long userId = getUser();
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(mentoringTeamId).orElseThrow(MentoringTeamNotFoundException::new);
        Optional<MentoringParticipation> teamLeader = mentoringParticipationRepository.findDynamicMentoringParticipation(
                mentoringTeam, user, MentoringAuthority.LEADER,MentoringParticipationStatus.ACCEPTED,null,false);
        if (teamLeader.isPresent()) {
            mentoringTeam.flag(Status.TRUE);
            mentoringBoardRepository.deleteByTeamId(mentoringTeamId);
            // 영속성 컨텍스트 초기화
            entityManager.clear();
        }
        else throw new NoAuthorityException("삭제 할 권한이 없습니다");
    }


    /**
     * 멘토링팀 responseDto 반환로직
     * 팀구성원이면 team정보만 반환,
     * 일반사용자용 페이지는 지원현황 포함해서 반환
     * @param team
     * @return
     */
    @Transactional(readOnly = true)
    public TeamAuthorityResponse getMentoringTeam(MentoringTeam team) {
        // 로그인 여부 확인 메서드
        Long userId = getOptionalUser();

        TeamResponse dto = team.toDto();
        List<String> categories = team.getCategories().stream()
                .map(o -> String.valueOf(o.getCategory().getId()))
                .collect(Collectors.toList());
        dto.setCategories(categories);

        //리스폰스 dto생성
        TeamAuthorityResponse teamResponseDto = new TeamAuthorityResponse();
        teamResponseDto.setDto(dto);

        // 로그인하지 않은 사용자
        if (userId == null) {
            handleNoAuthUser(teamResponseDto, team, null);
            return teamResponseDto;
        }

        //로그인 사용자
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        mentoringParticipationRepository.findDynamicMentoringParticipation(team, user,null,MentoringParticipationStatus.ACCEPTED,null,false)
                .ifPresentOrElse(
                        participation -> {
                            // 권한 설정
                            teamResponseDto.setAuthority(participation.getAuthority());
                        },
                        () -> handleNoAuthUser(teamResponseDto, team, user) // Participation이 없는 경우 처리
                );

        return teamResponseDto;
    }

    /**
     * 나의 멘토링 팀 반환 DTO
     * @param team
     * @return
     */
    @Transactional(readOnly = true)
    public TeamInfoResponse getMyTeam(MentoringTeam team) {
        Long userId = getUser();
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));

        TeamInfoResponse teamDto = new TeamInfoResponse(team.getId(),
                team.getName(),
                team.getStartDate(),
                team.getEndDate(),
                team.getStatus());

        //권한 반환하는 로직
        Optional<MentoringParticipation> teamUser = mentoringParticipationRepository.findDynamicMentoringParticipation(
                team, user,null, MentoringParticipationStatus.ACCEPTED,null,false);

        if (teamUser.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_A_MEMBER_OF_TEAM);
        } else {
            teamDto.setAuthority(teamUser.get().getAuthority());
        }
        return teamDto;
    }


    private Long getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        return securityUser.getUserId();
    }

    /**
     * 로그인 하지 않은 사용자면 null반환
     * @return
     */
    private Long getOptionalUser() {
        try {
            return getUser(); // getUser() 호출
        } catch (Exception e) {
            return null; // 로그인하지 않은 경우 null 반환
        }
    }

    private void handleNoAuthUser(TeamAuthorityResponse teamResponseDto, MentoringTeam team, User user) {
        teamResponseDto.setAuthority(MentoringAuthority.NoAuth);
        List<ParticipationForUserResponse> forUser = mentoringParticipationRepository.findAllForUser(
                team.getId(), MentoringAuthority.LEADER);
        if (user != null) {
            setLoginStatus(forUser, user.getId());
        }
        teamResponseDto.setUserParticipations(forUser);
    }

    /**
     * MentoringStatus의 상태를 자동 변경
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    @Transactional
    public void updateMentoringStatus() {
        mentoringTeamRepository.updateStatusToWorking(MentoringStatus.WORKING, MentoringStatus.RECRUITING);
        mentoringTeamRepository.updateStatusToComplete(MentoringStatus.COMPLETE, MentoringStatus.WORKING);
        entityManager.clear();
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
