package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.entity.*;
import com.project.Teaming.domain.mentoring.repository.MentoringParticipationRepository;
import com.project.Teaming.domain.mentoring.repository.MentoringTeamRepository;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.error.exception.MentoringParticipationNotFoundException;
import com.project.Teaming.global.error.exception.MentoringTeamNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MentoringParticipationService {

    private final MentoringParticipationRepository mentoringParticipationRepository;
    private final MentoringTeamRepository mentoringTeamRepository;
    private final UserRepository userRepository;

    @Transactional
    public void saveMentoringParticipation(Long userId, Long mentoringTeamId, MentoringRole role) {
        User findUser = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));
        MentoringParticipation mentoringParticipation = MentoringParticipation.builder()
                .participationStatus(MentoringParticipationStatus.PENDING)
                .authority(MentoringAuthority.CREW)
                .role(role)
                .reportingCnt(0)
                .build();
        mentoringParticipation.setUser(findUser);
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(mentoringTeamId).orElseThrow(MentoringTeamNotFoundException::new);
        mentoringParticipation.addMentoringMember(mentoringTeam);
        mentoringParticipationRepository.save(mentoringParticipation);
    }

    public MentoringParticipation findMentoringParticipation(Long mentoringParticipationId) {
        return mentoringParticipationRepository.findById(mentoringParticipationId).orElseThrow(MentoringParticipationNotFoundException::new);
    }

    public List<MentoringParticipation> findAllMentoringParticipation() {
        return mentoringParticipationRepository.findAll();
    }
    public List<MentoringParticipation> findAllExistingMentoringParticipation(MentoringTeam team, MentoringParticipationStatus status) {
        return mentoringParticipationRepository.findAllByMemberStatus(team, status);
    }

    @Transactional
    public void deleteMentoringParticipation(Long userId) {

    }
}
