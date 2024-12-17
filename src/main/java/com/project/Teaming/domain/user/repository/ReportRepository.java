package com.project.Teaming.domain.user.repository;

import com.project.Teaming.domain.project.entity.ProjectParticipation;
import com.project.Teaming.domain.user.entity.Report;
import com.project.Teaming.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByProjectParticipationAndReportedUser(ProjectParticipation reporter, User reportedUser);
}
