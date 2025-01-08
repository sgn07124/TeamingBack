package com.project.Teaming.domain.mentoring.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static com.project.Teaming.domain.mentoring.entity.QCategory.*;
import static com.project.Teaming.domain.mentoring.entity.QTeamCategory.*;

@Repository
public class CategoryRepositoryCustomImpl implements CategoryRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public CategoryRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<String> findCategoryIdsByTeamId(Long teamId) {
        return queryFactory
                .select(category.id)
                .from(teamCategory)
                .join(teamCategory.category, category)
                .where(teamCategory.mentoringTeam.id.eq(teamId))
                .distinct()
                .fetch()
                .stream()
                .map(String::valueOf) // Long -> String 변환
                .collect(Collectors.toList());
    }
}
