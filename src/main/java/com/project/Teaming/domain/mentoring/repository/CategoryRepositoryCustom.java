package com.project.Teaming.domain.mentoring.repository;

import com.project.Teaming.domain.mentoring.dto.response.CategoryDto;

import java.util.List;
import java.util.Map;

public interface CategoryRepositoryCustom {

    List<String> findCategoryIdsByTeamId(Long teamId);

    List<CategoryDto> findCatgoriesByTeamIds(List<Long> teamIds);
}
