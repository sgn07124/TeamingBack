package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.dto.response.MentoringCategoryDto;
import com.project.Teaming.domain.mentoring.entity.Category;
import com.project.Teaming.domain.mentoring.repository.CategoryRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public MentoringCategoryDto findCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_SUCH_CATEGORY));
        return new MentoringCategoryDto(category.getId(),category.getName());
    }

    public List<MentoringCategoryDto> findAllCategory() {
        return categoryRepository.findAll()
                .stream()
                .map(category -> new MentoringCategoryDto(category.getId(), category.getName()))
                .toList();
    }
}
