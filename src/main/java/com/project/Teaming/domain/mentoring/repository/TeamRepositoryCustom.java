package com.project.Teaming.domain.mentoring.repository;

import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.user.entity.User;

import java.util.List;

public interface TeamRepositoryCustom {

    void updateStatusToWorking(MentoringStatus workingStatus, MentoringStatus recruitingStatus);

    void updateStatusToComplete(MentoringStatus completeStatus, MentoringStatus workingStatus);

    List<MentoringTeam> findTeamsWithStatusAndUser(User user, MentoringParticipationStatus status);
}
