package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.dto.request.RqTeamDto;
import com.project.Teaming.domain.mentoring.dto.response.MyTeamDto;
import com.project.Teaming.domain.mentoring.dto.response.TeamResponseDto;
import com.project.Teaming.domain.mentoring.dto.response.RsTeamDto;
import com.project.Teaming.domain.mentoring.entity.*;
import com.project.Teaming.domain.mentoring.repository.CategoryRepository;
import com.project.Teaming.domain.mentoring.repository.MentoringTeamRepository;
import com.project.Teaming.domain.mentoring.repository.TeamCategoryRepository;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.error.exception.MentoringTeamNotFoundException;
import com.project.Teaming.global.error.exception.NoAuthorityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MentoringTeamService {

    private final MentoringTeamRepository mentoringTeamRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TeamCategoryRepository teamCategoryRepository;

    /**
     * 멘토링팀 생성, 저장 로직
     * @param userId
     * @param dto
     */

    @Transactional
    public Long saveMentoringTeam(Long userId, RqTeamDto dto) {
        User findUser = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));
        //participation 생성 로직
        MentoringParticipation mentoringParticipation = MentoringParticipation.builder()
                .participationStatus(MentoringParticipationStatus.ACCEPTED)
                .authority(MentoringAuthority.LEADER)
                .role(dto.getRole())
                .reportingCnt(0)
                .build();


        //팀 생성 로직
        MentoringTeam mentoringTeam = MentoringTeam.builder()
                .name(dto.getName())
                .deadline(dto.getDeadline())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .mentoringCnt(dto.getMentoringCnt())
                .content(dto.getContent())
                .status(MentoringStatus.RECRUITING)
                .link(dto.getLink())
                .flag(Status.FALSE)
                .build();

        //연관관계 매핑
        mentoringParticipation.setUser(findUser);
        mentoringParticipation.addMentoringTeam(mentoringTeam);
        MentoringTeam saved = mentoringTeamRepository.save(mentoringTeam);

        //카테고리 생성
        List<Long> categoryIds = dto.getCategories();
        List<Category> categories = categoryRepository.findAllById(categoryIds);

        //연관관계 매핑
        for (Category category : categories) {
            TeamCategory teamCategory = new TeamCategory();
            teamCategory.setCategory(category);
            teamCategory.setMentoringTeam(mentoringTeam);
            teamCategoryRepository.save(teamCategory);
        }
        return saved.getId();
    }

    /**
     * 멘토링 팀 수정 로직, 팀 구성원이며 팀장이여야 가능하다
     * @param userId
     * @param mentoringTeamId
     * @param dto
     */
    @Transactional
    public void updateMentoringTeam(Long userId,Long mentoringTeamId, RqTeamDto dto) {
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(mentoringTeamId).orElseThrow(MentoringTeamNotFoundException::new);
        if (mentoringTeam.getFlag() == Status.TRUE) {
            throw new NoAuthorityException("이미 삭제된 팀 입니다.");
        }
        if (isTrue(userId, mentoringTeamId)) {
            mentoringTeam.mentoringTeamUpdate(dto); //업데이트 메서드
            List<TeamCategory> categoriesToRemove = new ArrayList<>(mentoringTeam.getCategories()); // 리스트 복사,객체 참조
            // 연관관계 해제
            for (TeamCategory teamCategory : categoriesToRemove) {
                teamCategory.removeCategory(teamCategory.getCategory());  // 팀 카테고리 연관관계 해제
                teamCategory.removeMentoringTeam(mentoringTeam);          // 멘토링 팀 연관관계 해제
            }

            // 팀에서 TeamCategory 컬렉션 초기화(안정성 보장)
            mentoringTeam.getCategories().clear();

            // TeamCategory 삭제
            for (TeamCategory teamCategory : categoriesToRemove) {
                teamCategoryRepository.delete(teamCategory);
            }

            //업데이트 될 카테고리들
            List<Long> categoriesId = dto.getCategories();
            List<Category> updateCategories = categoryRepository.findAllById(categoriesId);
            //연관관계 매핑
            for (Category category : updateCategories) {
                TeamCategory teamCategory = new TeamCategory();
                teamCategory.setCategory(category);
                teamCategory.setMentoringTeam(mentoringTeam);
                teamCategoryRepository.save(teamCategory);
            }

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
     * @param userId
     * @return
     */
    public List<MentoringTeam> findMyMentoringTeams(Long userId) {
        List<MentoringTeam> list = new ArrayList<>();
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("유저가 존재하지 않습니다"));
        List<MentoringParticipation> mentoringParticipants = user.getMentoringParticipations();
        for (MentoringParticipation x : mentoringParticipants) {
            if (x.getParticipationStatus() == MentoringParticipationStatus.ACCEPTED) {
                list.add(x.getMentoringTeam());
            }
        }
        return list;
    }


    /**
     * 멘토링 팀 삭제 로직, 팀 구성원이고 리더여야 가능하다
     * @param userId
     * @param mentoringTeamId
     */
    @Transactional
    public void deleteMentoringTeam(Long userId,Long mentoringTeamId) {
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(mentoringTeamId).orElseThrow(MentoringTeamNotFoundException::new);
        if (isTrue(userId, mentoringTeamId)) {
            mentoringTeam.setFlag(Status.TRUE);
        }
        else throw new NoAuthorityException("삭제 할 권한이 없습니다");
    }


    /**
     * 내가 속해있는 멘토링 팀이고 팀장인지 확인하는 검증 로직
     * 수정, 삭제시 사용
     * @param userId
     * @param mentoringTeamId
     * @return
     */
    private boolean isTrue(Long userId, Long mentoringTeamId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("유저가 존재하지 않습니다"));
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(mentoringTeamId).orElseThrow(MentoringTeamNotFoundException::new);
        List<MentoringParticipation> mentoringParticipants = user.getMentoringParticipations();
        for (MentoringParticipation x : mentoringParticipants) {
            if (x.getMentoringTeam() == mentoringTeam && x.getAuthority() == MentoringAuthority.LEADER) return true;
        }
        return false;
    }

    /**
     * 멘토링팀 responseDto 반환로직
     * 팀 정보만 일단 반환해주고 participation쪽 완성되면 권한에따라서 다르게 보내는 메서드 생성
     * @param team
     * @return
     */
    public TeamResponseDto getMentoringTeam(Long userId, MentoringTeam team) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));

        RsTeamDto dto = team.toDto();
        List<String> categories = team.getCategories().stream()
                .map(o -> o.getCategory().getName())
                .collect(Collectors.toList());
        dto.setCategories(categories);

        //리스폰스 dto생성
        TeamResponseDto teamResponseDto = new TeamResponseDto();

        //권한 반환하는 로직
        List<MentoringParticipation> mentoringParticipations = user.getMentoringParticipations();
        List<MentoringParticipation> collect = mentoringParticipations.stream()
                .filter(o -> o.getMentoringTeam().equals(team))
                .toList();

        if (collect.isEmpty()) {
            teamResponseDto.setAuthority(MentoringAuthority.NoAuth);
        } else {
            teamResponseDto.setAuthority(collect.get(0).getAuthority());
        }
        teamResponseDto.setDto(dto);

        return teamResponseDto;
    }

    /**
     * 나의 멘토링 팀 반환 DTO
     * @param userId
     * @param team
     * @return
     */
    public MyTeamDto getMyTeam(Long userId, MentoringTeam team) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));

        MyTeamDto teamDto = new MyTeamDto(team.getId(),
                team.getName(),
                team.getStartDate(),
                team.getEndDate(),
                team.getStatus());

        //권한 반환하는 로직
        List<MentoringParticipation> mentoringParticipations = user.getMentoringParticipations();
        List<MentoringParticipation> collect = mentoringParticipations.stream()
                .filter(o -> o.getMentoringTeam().equals(team))
                .toList();

        if (collect.isEmpty()) {
            teamDto.setAuthority(MentoringAuthority.NoAuth);
        } else {
            teamDto.setAuthority(collect.get(0).getAuthority());
        }

        return teamDto;
    }
}
