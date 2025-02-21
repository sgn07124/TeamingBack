package com.project.Teaming.domain.mentoring.repository;

import com.project.Teaming.domain.mentoring.entity.*;
import com.project.Teaming.domain.user.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class TeamRepositoryCustomImpl implements TeamRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public TeamRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public void updateStatusToWorking(MentoringStatus workingStatus, MentoringStatus recruitingStatus) {
        QMentoringTeam mentoringTeam = QMentoringTeam.mentoringTeam;

        queryFactory
                .update(mentoringTeam)
                .set(mentoringTeam.status, workingStatus)
                .where(
                        mentoringTeam.status.eq(recruitingStatus)
                                .and(mentoringTeam.startDate.loe(LocalDate.now())) // startDate <= CURRENT_DATE
                                .and(mentoringTeam.endDate.gt(LocalDate.now()))    // endDate > CURRENT_DATE
                )
                .execute();
    }

    @Override
    public void updateStatusToComplete(MentoringStatus completeStatus, MentoringStatus workingStatus) {
        QMentoringTeam mentoringTeam = QMentoringTeam.mentoringTeam;

        queryFactory
                .update(mentoringTeam)
                .set(mentoringTeam.status, completeStatus)
                .where(
                        mentoringTeam.status.eq(workingStatus)
                                .and(mentoringTeam.endDate.lt(LocalDate.now())) // endDate < CURRENT_DATE
                )
                .execute();
    }



}
