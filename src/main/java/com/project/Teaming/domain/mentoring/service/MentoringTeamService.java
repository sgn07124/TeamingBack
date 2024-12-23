package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.dto.request.RqParticipationDto;
import com.project.Teaming.domain.mentoring.dto.request.RqTeamDto;
import com.project.Teaming.domain.mentoring.dto.response.*;
import com.project.Teaming.domain.mentoring.entity.*;
import com.project.Teaming.domain.mentoring.repository.*;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.error.exception.MentoringTeamNotFoundException;
import com.project.Teaming.global.error.exception.NoAuthorityException;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import java.util.stream.Collectors;

import static org.springframework.data.util.Optionals.ifPresentOrElse;


@Slf4j
@Service
@Transactional(readOnly = true)
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
    public Long saveMentoringTeam(RqTeamDto dto) {

        MentoringTeam mentoringTeam = MentoringTeam.builder()
                .name(dto.getName())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .mentoringCnt(dto.getMentoringCnt())
                .content(dto.getContent())
                .status(MentoringStatus.RECRUITING)
                .link(dto.getLink())
                .flag(Status.FALSE)
                .build();

        MentoringTeam saved = mentoringTeamRepository.save(mentoringTeam);
        //리더 생성
        RqParticipationDto participationDto = new RqParticipationDto(MentoringAuthority.LEADER, MentoringParticipationStatus.ACCEPTED, dto.getRole());
        mentoringParticipationService.saveMentoringParticipation(mentoringTeam, participationDto);

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
    public void updateMentoringTeam(Long mentoringTeamId, RqTeamDto dto) {
        User user = getUser();
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(mentoringTeamId).orElseThrow(MentoringTeamNotFoundException::new);
        Optional<MentoringParticipation> teamLeader = mentoringParticipationRepository.findByMentoringTeamAndUserAndAuthority(mentoringTeam, user, MentoringAuthority.LEADER);
        if (mentoringTeam.getFlag() == Status.TRUE) {
            throw new NoAuthorityException("이미 삭제된 팀 입니다.");
        }
        if (teamLeader.isPresent() && !teamLeader.get().getIsDeleted()) {
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
    public List<MentoringTeam> findMyMentoringTeams() {
        User user = getUser();
        List<MentoringParticipation> participations = mentoringParticipationRepository.findParticipationsWithTeamsAndUser(
                user,
                MentoringParticipationStatus.ACCEPTED,
                Status.TRUE
        );
        return participations.stream()
                .map(MentoringParticipation::getMentoringTeam)
                .toList();
    }


    /**
     * 멘토링 팀 삭제 로직, 팀 구성원이고 리더여야 가능하다
     * @param mentoringTeamId
     */
    @Transactional
    public void deleteMentoringTeam(Long mentoringTeamId) {
        User user = getUser();
        MentoringTeam mentoringTeam = mentoringTeamRepository.findWithBoardsById(mentoringTeamId).orElseThrow(MentoringTeamNotFoundException::new);
        Optional<MentoringParticipation> teamLeader = mentoringParticipationRepository.findByMentoringTeamAndUserAndAuthority(mentoringTeam, user, MentoringAuthority.LEADER);
        if (teamLeader.isPresent() && !teamLeader.get().getIsDeleted()) {
            mentoringTeam.setFlag(Status.TRUE);
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
    public TeamResponseDto getMentoringTeam( MentoringTeam team) {
        // 로그인 여부 확인 메서드
        User user = getOptionalUser();

        RsTeamDto dto = team.toDto();
        List<String> categories = team.getCategories().stream()
                .map(o -> String.valueOf(o.getCategory().getId()))
                .collect(Collectors.toList());
        dto.setCategories(categories);

        //리스폰스 dto생성
        TeamResponseDto teamResponseDto = new TeamResponseDto();
        teamResponseDto.setDto(dto);

        // 로그인하지 않은 사용자
        if (user == null) {
            handleNoAuthUser(teamResponseDto, team, null);
            return teamResponseDto;
        }
        //로그인 사용자
        mentoringParticipationRepository.findByMentoringTeamAndUser(team, user)
        .ifPresentOrElse(
                participation -> {
                    if (participation.getParticipationStatus() == MentoringParticipationStatus.ACCEPTED && !participation.getIsDeleted()) {
                        teamResponseDto.setAuthority(participation.getAuthority());
                    } else handleNoAuthUser(teamResponseDto, team, user);
                },
                () -> handleNoAuthUser(teamResponseDto, team, user)
        );
        return teamResponseDto;
    }

    /**
     * 나의 멘토링 팀 반환 DTO
     * @param team
     * @return
     */
    public MyTeamDto getMyTeam(MentoringTeam team) {
        User user = getUser();

        MyTeamDto teamDto = new MyTeamDto(team.getId(),
                team.getName(),
                team.getStartDate(),
                team.getEndDate(),
                team.getStatus());

        //권한 반환하는 로직
        Optional<MentoringParticipation> teamUser = mentoringParticipationRepository.findByMentoringTeamAndUser(team, user);

        if (teamUser.isEmpty()) {
            teamDto.setAuthority(MentoringAuthority.NoAuth);
        } else {
            teamDto.setAuthority(teamUser.get().getAuthority());
        }
        return teamDto;
    }


    private User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        Long userId = securityUser.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        return user;
    }

    /**
     * 로그인 하지 않은 사용자면 null반환
     * @return
     */
    private User getOptionalUser() {
        try {
            return getUser(); // getUser() 호출
        } catch (Exception e) {
            return null; // 로그인하지 않은 경우 null 반환
        }
    }

    private void handleNoAuthUser(TeamResponseDto teamResponseDto, MentoringTeam team, User user) {
        teamResponseDto.setAuthority(MentoringAuthority.NoAuth);
        List<RsUserParticipationDto> forUser = mentoringParticipationRepository.findAllForUser(
                team.getId(), MentoringAuthority.LEADER, MentoringParticipationStatus.EXPORT);
        if (user != null) {
            setLoginStatus(forUser, user.getId());
        }
        teamResponseDto.setUserParticipations(forUser);
    }

    /**
     * 로그인 한 사용자 있는지 확인하는 로직
     * @param dtos
     * @param userId
     */
    private void setLoginStatus(List<?> dtos, Long userId) {
        dtos.forEach(dto -> {
            if (dto instanceof RsTeamUserDto teamDto && teamDto.getUserId().equals(userId)) {
                teamDto.setIsLogined(true);
            } else if (dto instanceof RsUserParticipationDto userDto && userDto.getUserId().equals(userId)) {
                userDto.setIsLogined(true);
            }
        });
    }
}
