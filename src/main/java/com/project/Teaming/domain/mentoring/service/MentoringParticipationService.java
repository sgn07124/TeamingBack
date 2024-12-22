package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.dto.request.ReportDto;
import com.project.Teaming.domain.mentoring.dto.request.RqParticipationDto;
import com.project.Teaming.domain.mentoring.dto.response.*;
import com.project.Teaming.domain.mentoring.entity.*;
import com.project.Teaming.domain.mentoring.repository.MentoringParticipationRepository;
import com.project.Teaming.domain.mentoring.repository.MentoringTeamRepository;
import com.project.Teaming.domain.user.entity.Report;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.ReportRepository;
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
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MentoringParticipationService {

    private final MentoringParticipationRepository mentoringParticipationRepository;
    private final MentoringTeamRepository mentoringTeamRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    /**
     * 지원자로 등록하는 로직
     * @param dto
     * @return
     */
    @Transactional
    public Long saveMentoringParticipation(MentoringTeam mentoringTeam, RqParticipationDto dto) {
        User user = getUser();
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
                    .participationStatus(dto.getStatus())
                    .authority(dto.getAuthority())
                    .role(dto.getRole())
                    .requestDate(LocalDateTime.now())
                    .reportingCnt(0)
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
     * 강퇴하는 로직
     * @param teamId
     * @param userId
     */

    @Transactional
    public void exportTeamUser(Long teamId, Long userId) {
        User user = getUser();
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(teamId).orElseThrow(MentoringTeamNotFoundException::new);
        User exportUser = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        Optional<MentoringParticipation> teamLeader = mentoringParticipationRepository.findByMentoringTeamAndUserAndAuthority(mentoringTeam, user, MentoringAuthority.LEADER);
        if (teamLeader.isEmpty()) {
            throw new NoAuthorityException(ErrorCode.NOT_A_LEADER);
        }
        Optional<MentoringParticipation> export = mentoringParticipationRepository.findByMentoringTeamAndUser(mentoringTeam, exportUser);
        if (export.isPresent()) {
            if (export.get().getAuthority() == MentoringAuthority.CREW && export.get().getParticipationStatus() == MentoringParticipationStatus.ACCEPTED) {
                export.get().setParticipationStatus(MentoringParticipationStatus.EXPORT);
            } else {
                throw new BusinessException(ErrorCode.NOT_A_MEMBER);
            }
        }
        else throw new BusinessException(ErrorCode.EXPORTED_MEMBER_NOT_EXISTS);
    }

    @Transactional
    public void reportTeamUser(ReportDto dto) {
        // 신고자
        User reporter = getUser();
        //관련된 팀
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(dto.getTeamId())
                .orElseThrow(MentoringTeamNotFoundException::new);
        //신고한 teamParticipation
        //신고자가 팀 구성원인지 확인
        MentoringParticipation reportingParticipation = mentoringParticipationRepository.findByMentoringTeamAndUserAndParticipationStatus(mentoringTeam, reporter, MentoringParticipationStatus.ACCEPTED)
                .orElseThrow(() ->new BusinessException(ErrorCode.NOT_A_TEAM_USER));
        //신고당한 사용자
        User reportedUser = userRepository.findById(dto.getReportedUserId())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        //신고당한 teamParticipation
        MentoringParticipation reportedParticipation = mentoringParticipationRepository.findByMentoringTeamAndUser(mentoringTeam, reportedUser)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REPORT_TARGET));

        // 이미 신고한 사용자인지 확인
        boolean reportExists = reportRepository.existsByMentoringParticipationAndReportedUser(reportingParticipation, reportedUser);
        if (reportExists) {
            throw new BusinessException(ErrorCode.ALREADY_REPORTED);
        }
        // 자기자신에 대해서 하는 경우 예외처리
        if (reporter.getId().equals(dto.getReportedUserId())) {
            throw new BusinessException(ErrorCode.INVALID_SELF_ACTION);
        }
        // 신고대상이 강퇴된 사용자거나, 탈퇴한 사용자인 경우 신고진행
        if (reportedParticipation.getParticipationStatus() == MentoringParticipationStatus.EXPORT || reportedParticipation.getIsDeleted()) {
            Report report = Report.mentoringReport(reportingParticipation, reportedUser);
            reportRepository.save(report);
            reportedParticipation.setReportingCnt(reportedParticipation.getReportingCnt() + 1);
            updateReportedWarningCount(reportedParticipation);
        }
        else throw new BusinessException(ErrorCode.STILL_TEAM_USER);
    }

    @Transactional
    public void updateReportedWarningCount(MentoringParticipation reportedParticipation) {
        Long teamId = reportedParticipation.getMentoringTeam().getId();
        Long reportedUserId = reportedParticipation.getUser().getId();

        // 경고 처리 여부 확인
        if (reportedParticipation.getWarningProcessed()) {
            return; // 이미 처리된 경우 중복 처리 방지
        }
        // 팀원 수 조회
        long totalMembers = mentoringParticipationRepository.countByMentoringTeamIdAndParticipationStatusAndIsDeleted(teamId, MentoringParticipationStatus.ACCEPTED);

        // 과반수 이상의 신고 횟수인지 확인
        if (reportedParticipation.getReportingCnt() >= Math.ceil(totalMembers / 2.0)) {
            // 경고 횟수 증가
            User reportedUser = reportedParticipation.getUser();
            reportedUser.incrementWarningCnt();
            userRepository.save(reportedUser);

            // 경고 처리 상태 업데이트
            reportedParticipation.setWarningProcessed(true);
            mentoringParticipationRepository.save(reportedParticipation);
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
            if (teamUser.get().getAuthority() == MentoringAuthority.LEADER) {
                //제일 일찍 들어온 팀원 조회
                Optional<MentoringParticipation> firstMember = mentoringParticipationRepository.findTeamUsers(mentoringTeam.getId(), MentoringParticipationStatus.ACCEPTED, MentoringAuthority.CREW)
                        .stream()
                        .findFirst();
                // 새로운 리더 설정
                firstMember.ifPresentOrElse(
                        participation -> {
                            participation.setAuthority(MentoringAuthority.LEADER);
                        },
                        () -> {
                            throw new BusinessException(ErrorCode.NO_ELIGIBLE_MEMBER_FOR_LEADER);
                        }
                );
            }
        } else {
            throw new NoAuthorityException(ErrorCode.NOT_A_MEMBER);
        }
    }

    /**
     * 권한별로 팀원과 지원자 데이터를 반환하는 로직
     * 리더용(팀원, 지원자현황)
     * 팀원용(팀원)
     * 일반사용자 또는 지원자용(지원자현황)
     * @param teamId
     * @return
     */

    public ParticipantsDto<?> getParticipantsInfo(Long teamId) {
        User user = getUser();
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(teamId).orElseThrow(MentoringTeamNotFoundException::new);
        Optional<MentoringParticipation> teamUser = mentoringParticipationRepository.findByMentoringTeamAndUser(mentoringTeam,user);
        if (teamUser.isPresent()) {  //팀과의 연관관계가 있으면
            if (teamUser.get().getAuthority() == MentoringAuthority.LEADER && !teamUser.get().getIsDeleted()) {  //팀의 리더인 유저
                List<RsTeamUserDto> allTeamUsers = mentoringParticipationRepository.findAllByMemberStatus(mentoringTeam, MentoringParticipationStatus.ACCEPTED, MentoringParticipationStatus.EXPORT);
                setLoginStatus(allTeamUsers,user.getId());
                List<RsTeamParticipationDto> participations = mentoringParticipationRepository.findAllForLeader(teamId, 1L, MentoringParticipationStatus.EXPORT);
                LeaderResponseDto dto = new LeaderResponseDto();
                dto.setMembers(allTeamUsers);
                dto.setParticipations(participations);
                List<Object> responseList = new ArrayList<>();
                responseList.add(MentoringAuthority.LEADER);
                responseList.add(dto);
                return new ParticipantsDto<>(responseList);

            } else if (teamUser.get().getAuthority() == MentoringAuthority.CREW && !teamUser.get().getIsDeleted() && teamUser.get().getParticipationStatus() != MentoringParticipationStatus.EXPORT) {  //팀의 멤버인 유저
                List<RsTeamUserDto> members = mentoringParticipationRepository.findAllByMemberStatus(mentoringTeam, MentoringParticipationStatus.ACCEPTED, MentoringParticipationStatus.EXPORT);
                setLoginStatus(members,user.getId());
                List<Object> responseList = new ArrayList<>();
                responseList.add(MentoringAuthority.CREW);
                responseList.add(members);
                return new ParticipantsDto<>(responseList);
            }
        }
        //연관관계가 있지만 NoAuth인 경우 또는 연관관계가 없는경우
        List<RsUserParticipationDto> forUser = mentoringParticipationRepository.findAllForUser(teamId, 1L, MentoringParticipationStatus.EXPORT);
        setLoginStatus(forUser,user.getId());
        List<Object> responseList = new ArrayList<>();
        responseList.add(MentoringAuthority.NoAuth);
        responseList.add(forUser);
        return new ParticipantsDto<>(responseList);
    }

    private User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        Long userId = securityUser.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        return user;
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
