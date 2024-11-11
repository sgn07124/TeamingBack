package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.dto.request.RqTeamDto;
import com.project.Teaming.domain.mentoring.dto.response.RsParticipationDto;
import com.project.Teaming.domain.mentoring.dto.response.TeamResponseDto;
import com.project.Teaming.domain.mentoring.dto.response.RsTeamDto;
import com.project.Teaming.domain.mentoring.entity.*;
import com.project.Teaming.domain.mentoring.repository.MentoringParticipationRepository;
import com.project.Teaming.domain.mentoring.repository.MentoringTeamRepository;
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
    private final MentoringParticipationRepository mentoringParticipationRepository;

    /**
     * 멘토링팀 생성, 저장 로직
     * @param userId
     * @param role
     * @param dto
     */

    @Transactional
    public void saveMentoringTeam(Long userId, MentoringRole role, RqTeamDto dto) {
        User findUser = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));
        MentoringParticipation mentoringParticipation = MentoringParticipation.builder()
                .participationStatus(MentoringParticipationStatus.ACCEPTED)
                .authority(MentoringAuthority.LEADER)
                .role(role)
                .reportingCnt(0)
                .build();

        mentoringParticipation.setUser(findUser);
        MentoringTeam mentoringTeam = MentoringTeam.builder()
                .name(dto.getName())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .mentoringCnt(dto.getMentoringCnt())
                .content(dto.getContent())
                .status(MentoringStatus.RECRUITING)
                .link(dto.getLink())
                .flag(Status.FALSE)
                .mentoringParticipationList(new ArrayList<>())
                .mentoringBoardList(new ArrayList<>())
                .eventList(new ArrayList<>())
                .build();

        mentoringParticipation.addMentoringMember(mentoringTeam);
        mentoringParticipationRepository.save(mentoringParticipation);
        mentoringTeamRepository.save(mentoringTeam);

    }

    /**
     * 멘토링 팀 수정 로직, 인원 제외
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
            mentoringTeam.mentoringTeamUpdate(dto);
        }
        else throw new NoAuthorityException();
    }


    /**
     * 특정 멘토링 팀을 찾는 로직
     * @param mentoringTeamId
     * @return
     */
    public MentoringTeam findMentoringTeam(Long mentoringTeamId) {
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(mentoringTeamId).orElseThrow(MentoringTeamNotFoundException::new);
        if (mentoringTeam.getFlag() == Status.TRUE) {
            throw new NoAuthorityException("이미 삭제된 팀 입니다.");
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
            list.add(x.getMentoringTeam());
        }
        return list;
    }


    /**
     * 멘토링 팀 삭제 로직
     * @param userId
     * @param mentoringTeamId
     */
    @Transactional
    public void deleteMentoringTeam(Long userId,Long mentoringTeamId) {
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(mentoringTeamId).orElseThrow(MentoringTeamNotFoundException::new);
        if (isTrue(userId, mentoringTeamId)) {
            mentoringTeam.setFlag(Status.TRUE);
        }
        else throw new NoAuthorityException();
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
     * @param team
     * @return
     */
    public TeamResponseDto getMentoringTeam(MentoringTeam team) {
        RsTeamDto dto = team.toDto();
        List<MentoringParticipation> members = mentoringParticipationRepository.findAllByMemberStatus(team, MentoringParticipationStatus.ACCEPTED);
        List<RsParticipationDto> users = members.stream()
                .map(o -> RsParticipationDto.builder()
                        .id(o.getId())
                        .role(o.getRole())
                        .authority(o.getAuthority())
                        .build())
                .collect(Collectors.toList());
        TeamResponseDto teamResponseDto = new TeamResponseDto();
        teamResponseDto.setDto(dto);
        teamResponseDto.setUserDto(users);
        return teamResponseDto;
    }

}
