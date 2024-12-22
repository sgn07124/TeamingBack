package com.project.Teaming.domain.mentoring.repository;

import com.project.Teaming.domain.mentoring.entity.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BoardRepositoryCustomImpl implements BoardRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public BoardRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Long> findMentoringBoardIds(Long lastCursor, int size, Status flag) {
        QMentoringBoard mb = QMentoringBoard.mentoringBoard;
        QMentoringTeam mt = QMentoringTeam.mentoringTeam;

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

        QMentoringBoard mb = QMentoringBoard.mentoringBoard;
        QMentoringTeam mt = QMentoringTeam.mentoringTeam;
        QTeamCategory tc = QTeamCategory.teamCategory;
        QCategory c = QCategory.category;

        return queryFactory
                .selectFrom(mb)
                .join(mb.mentoringTeam, mt).fetchJoin()
                .join(mt.categories, tc).fetchJoin()
                .join(tc.category, c).fetchJoin()
                .where(mb.id.in(boardIds))
                .orderBy(mb.createdDate.desc(),
                        c.id.asc())             // Category 기준 정렬 추가
                .fetch();

    }

    private BooleanExpression lastCursorCondition(Long lastCursor) {
        QMentoringBoard mb = QMentoringBoard.mentoringBoard;
        return lastCursor == null ? null : mb.id.lt(lastCursor);
    }
}
