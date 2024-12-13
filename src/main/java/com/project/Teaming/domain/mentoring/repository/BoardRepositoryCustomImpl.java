package com.project.Teaming.domain.mentoring.repository;

import com.project.Teaming.domain.mentoring.dto.response.RsBoardDto;
import com.project.Teaming.domain.mentoring.entity.*;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BoardRepositoryCustomImpl implements BoardRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public BoardRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Long> findMentoringBoardIds(MentoringStatus status, Status flag, Pageable pageable) {
        QMentoringBoard mb = QMentoringBoard.mentoringBoard;
        QMentoringTeam mt = QMentoringTeam.mentoringTeam;

        return queryFactory
                .select(mb.id)
                .from(mb)
                .join(mb.mentoringTeam, mt)
                .where(mt.flag.eq(flag), statusEq(status))
                .orderBy(mb.createdDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<Tuple> findAllByIds(List<Long> boardIds) {
        if (boardIds.isEmpty()) {
            return List.of();
        }

        QMentoringBoard mb = QMentoringBoard.mentoringBoard;
        QMentoringTeam mt = QMentoringTeam.mentoringTeam;
        QTeamCategory tc = QTeamCategory.teamCategory;
        QCategory c = QCategory.category;

        return queryFactory
                .select(mb.id, mb.title, mt.name, mt.startDate, mt.endDate, c.id, mb.contents)
                .from(mb)
                .join(mb.mentoringTeam, mt)
                .leftJoin(mt.categories, tc)
                .leftJoin(tc.category, c)
                .where(mb.id.in(boardIds))
                .orderBy(mb.createdDate.desc())
                .fetch();

    }

    @Override
    public long countAllByStatus(MentoringStatus status, Status flag) {
        QMentoringBoard mb = QMentoringBoard.mentoringBoard;
        QMentoringTeam mt = QMentoringTeam.mentoringTeam;

        return queryFactory
                .select(mb.count())
                .from(mb)
                .join(mb.mentoringTeam, mt)
                .where(statusEq(status), mt.flag.eq(flag))
                .fetchOne();
    }

    private BooleanExpression statusEq(MentoringStatus status) {
        return status == null ? null : QMentoringTeam.mentoringTeam.status.eq(status);
    }
}
