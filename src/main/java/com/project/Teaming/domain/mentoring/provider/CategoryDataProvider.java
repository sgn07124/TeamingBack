package com.project.Teaming.domain.mentoring.provider;

import com.project.Teaming.domain.mentoring.dto.response.CategoryDto;
import com.project.Teaming.domain.mentoring.repository.CategoryRepository;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CategoryDataProvider {

    private final CategoryRepository categoryRepository;

    public Map<Long, List<String>> mapCategoriesByTeamIds(List<Long> teamIds) {

        return categoryRepository.findCatgoriesByTeamIds(teamIds)
                .stream()
                .collect(Collectors.groupingBy(
                        CategoryDto::getTeamId,
                        Collectors.mapping(CategoryDto::getCategoryId, Collectors.toList())
                ));
    }

}
