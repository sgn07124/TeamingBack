package com.project.Teaming.domain.mentoring.repository;

import com.project.Teaming.domain.mentoring.dto.response.ParticipationForUserResponse;
import com.project.Teaming.domain.mentoring.dto.response.TeamParticipationResponse;
import com.project.Teaming.domain.mentoring.dto.response.TeamUserResponse;
import com.project.Teaming.domain.mentoring.entity.*;
import com.project.Teaming.domain.user.entity.QReview;
import com.project.Teaming.domain.user.entity.QUser;
import com.project.Teaming.domain.user.entity.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ParticipationRepositoryCustomImpl implements ParticipationRepositoryCustom{

    JPAQueryFactory queryFactory;

    public ParticipationRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<TeamUserResponse> findAllByMemberStatus(
            MentoringTeam team,
            MentoringStatus teamStatus,
            MentoringParticipationStatus status,
            Long reviewerParticipationId) {

        QMentoringParticipation mp = QMentoringParticipation.mentoringParticipation;
        QUser u = QUser.user;
        QMentoringTeam mt = QMentoringTeam.mentoringTeam;
        QReview r = QReview.review;

        return queryFactory
                .select(Projections.constructor(
                        TeamUserResponse.class,
                        mp.decisionDate,
                        u.id,
                        u.name,
                        mp.role,
                        mp.participationStatus,
                        mp.isDeleted,
                        new CaseBuilder()
                                .when(mt.status.eq(teamStatus)
                                        .and(r.id.isNotNull()))
                                .then(true)
                                .otherwise(false)
                ))
                .from(mp)
                .join(mp.user, u)
                .join(mp.mentoringTeam, mt)
                .leftJoin(r)
                .on(r.reviewee.id.eq(u.id)
                        .and(r.mentoringParticipation.id.eq(reviewerParticipationId)))
                .where(
                        mt.eq(team),
                        mp.participationStatus.eq(status)
                )
                .orderBy(mp.decisionDate.asc())
                .fetch();
    }


    @Override
    public Optional<MentoringParticipation> findDynamicMentoringParticipation(MentoringTeam mentoringTeam, User user, MentoringAuthority authority,
                                                                              MentoringParticipationStatus status) {

        QMentoringParticipation mp = QMentoringParticipation.mentoringParticipation;

        BooleanBuilder builder = new BooleanBuilder();


        if (mentoringTeam != null) {
            builder.and(mp.mentoringTeam.eq(mentoringTeam));
        }

        if (user != null) {
            builder.and(mp.user.eq(user));
        }

        if (authority != null) {
            builder.and(mp.authority.eq(authority));
        }

        if (status != null) {
            builder.and(mp.participationStatus.eq(status));
        }

        MentoringParticipation result = queryFactory
                .selectFrom(mp)
                .where(builder)
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<MentoringParticipation> findFirstUser(Long teamId, MentoringParticipationStatus participationStatus, MentoringAuthority authority) {
        QMentoringParticipation mp = QMentoringParticipation.mentoringParticipation;
        QMentoringTeam mt = QMentoringTeam.mentoringTeam;

        return Optional.ofNullable(
                queryFactory
                        .selectFrom(mp)
                        .join(mp.mentoringTeam, mt).fetchJoin()
                        .where(
                                mt.id.eq(teamId),
                                mp.isDeleted.isFalse(),
                                mp.participationStatus.eq(participationStatus),
                                mp.authority.eq(authority)
                        )
                        .orderBy(mp.decisionDate.asc())
                        .fetchFirst()
        );
    }

    @Override
    public List<MentoringParticipation> findParticipationWithStatusAndUser(User user, MentoringParticipationStatus status) {
        QMentoringParticipation mp = QMentoringParticipation.mentoringParticipation;
        QMentoringTeam mt = QMentoringTeam.mentoringTeam;

        return queryFactory
                .select(mp)
                .from(mp)
                .join(mp.mentoringTeam, mt).fetchJoin()
                .where(
                        mp.user.eq(user),
                        mp.participationStatus.eq(status),
                        mp.isDeleted.isFalse(),
                        mt.flag.eq(Status.FALSE)
                )
                .distinct()
                .fetch();
    }


    @Override
    public long countBy(Long teamId, MentoringParticipationStatus status) {
        QMentoringParticipation mp = QMentoringParticipation.mentoringParticipation;

        return queryFactory
                .select(mp.count()) // COUNT(mp) 선택
                .from(mp)
                .where(
                        mp.mentoringTeam.id.eq(teamId), // teamId 조건
                        mp.participationStatus.eq(status) // participationStatus 조건
                )
                .fetchOne(); // 단일 값 반환
    }

}
