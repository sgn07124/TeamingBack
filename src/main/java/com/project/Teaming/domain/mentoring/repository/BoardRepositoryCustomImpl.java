package com.project.Teaming.domain.mentoring.repository;

import com.project.Teaming.domain.mentoring.entity.*;
import com.project.Teaming.domain.project.entity.QProjectBoard;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static com.project.Teaming.domain.mentoring.entity.QCategory.*;
import static com.project.Teaming.domain.mentoring.entity.QMentoringBoard.*;
import static com.project.Teaming.domain.mentoring.entity.QMentoringTeam.*;
import static com.project.Teaming.domain.mentoring.entity.QTeamCategory.*;

@Repository
public class BoardRepositoryCustomImpl implements BoardRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public BoardRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Long> findMentoringBoardIds(Long lastCursor, int size, Status flag) {
        QMentoringBoard mb = mentoringBoard;
        QMentoringTeam mt = mentoringTeam;

        return queryFactory
                .select(mb.id)
                .from(mb)
                .join(mb.mentoringTeam, mt)
                .where(
                        mt.flag.eq(flag),
                        lastCursorCondition(lastCursor)
                        )
                .orderBy(mb.createdDate.desc())
                .limit(size)
                .fetch();
    }

    @Override
    public List<MentoringBoard> findAllByIds(List<Long> boardIds) {
        if (boardIds.isEmpty()) {
            return List.of();
        }

        QMentoringBoard mb = mentoringBoard;
        QMentoringTeam mt = mentoringTeam;
        QTeamCategory tc = teamCategory;
        QCategory c = category;

        return queryFactory
                .selectDistinct(mb)
                .from(mb)
                .join(mb.mentoringTeam, mt).fetchJoin()
                .join(mt.categories, tc).fetchJoin()
                .join(tc.category, c).fetchJoin()
                .where(mb.id.in(boardIds))
                .orderBy(mb.createdDate.desc(),
                        c.id.asc())             // Category 기준 정렬 추가
                .fetch();

    }
    @Override
    public void deleteByTeamId(Long teamId) {
        QMentoringBoard mb = mentoringBoard;

        queryFactory
                .delete(mb)
                .where(mb.mentoringTeam.id.eq(teamId))
                .execute();
    }

    @Override
    public void bulkUpDateStatus(PostStatus newStatus, PostStatus currentStatus, LocalDate now) {
        QMentoringBoard mb = mentoringBoard;

        queryFactory
                .update(mb)
                .set(mb.status, newStatus)
                .where(
                        mb.status.eq(currentStatus)
                                .and(mb.deadLine.lt(now))
                )
                .execute();
    }



    private BooleanExpression lastCursorCondition(Long lastCursor) {
        QMentoringBoard mb = mentoringBoard;
        return lastCursor == null ? null : mb.id.lt(lastCursor);
    }
}
