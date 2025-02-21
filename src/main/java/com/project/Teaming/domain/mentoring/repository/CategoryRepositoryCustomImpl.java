package com.project.Teaming.domain.mentoring.repository;

import com.project.Teaming.domain.mentoring.dto.response.CategoryDto;
import com.project.Teaming.domain.mentoring.entity.QCategory;
import com.project.Teaming.domain.mentoring.entity.QTeamCategory;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
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

    @Override
    public List<CategoryDto> findCatgoriesByTeamIds(List<Long> teamIds) {
        if (teamIds.isEmpty()) {
            return List.of();
        }

        QCategory c = QCategory.category;
        QTeamCategory tc = QTeamCategory.teamCategory;

        // QueryDSL로 데이터 조회 및 DTO 매핑
        return queryFactory
                .select(Projections.constructor(
                        CategoryDto.class,
                        tc.mentoringTeam.id,
                        c.id.stringValue()
                ))
                .from(tc)
                .join(tc.category, c)
                .where(tc.mentoringTeam.id.in(teamIds))
                .fetch();
    }
}
