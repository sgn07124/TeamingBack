package com.project.Teaming.domain.mentoring.repository;

import java.util.List;

public interface CategoryRepositoryCustom {

    List<String> findCategoryIdsByTeamId(Long teamId);
}
