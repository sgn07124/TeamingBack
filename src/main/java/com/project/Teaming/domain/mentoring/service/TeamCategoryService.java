package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.entity.Category;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.mentoring.entity.TeamCategory;
import com.project.Teaming.domain.mentoring.repository.CategoryRepository;
import com.project.Teaming.domain.mentoring.repository.MentoringTeamRepository;
import com.project.Teaming.domain.mentoring.repository.TeamCategoryRepository;
import com.project.Teaming.global.error.exception.MentoringTeamNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamCategoryService {

    private final TeamCategoryRepository teamCategoryRepository;
    private final CategoryRepository categoryRepository;


    @Transactional
    public void saveTeamCategories(MentoringTeam mentoringTeam, List<Long> categoryIds) {
        List<Category> categories = categoryRepository.findAllById(categoryIds);

        //연관관계 매핑
        for (Category category : categories) {
            TeamCategory teamCategory = new TeamCategory();
            teamCategory.setCategory(category);
            teamCategory.setMentoringTeam(mentoringTeam);
            teamCategoryRepository.save(teamCategory);
        }
    }

    @Transactional
    public void removeTeamCategories(MentoringTeam mentoringTeam) {
        List<TeamCategory> categoriesToRemove = new ArrayList<>(mentoringTeam.getCategories());
        for (TeamCategory teamCategory : categoriesToRemove) {
            teamCategory.removeCategory(teamCategory.getCategory());
            teamCategory.removeMentoringTeam(mentoringTeam);
            mentoringTeam.getCategories().remove(teamCategory);  //안정성 보장
            teamCategoryRepository.delete(teamCategory);
        }
    }
}
