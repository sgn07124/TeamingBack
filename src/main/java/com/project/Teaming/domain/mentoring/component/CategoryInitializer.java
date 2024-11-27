package com.project.Teaming.domain.mentoring.component;

import com.project.Teaming.domain.mentoring.entity.Category;
import com.project.Teaming.domain.mentoring.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class CategoryInitializer {

    private final CategoryRepository repository;

    public CategoryInitializer(CategoryRepository repository) {
        this.repository = repository;
    }

    @Bean
    CommandLineRunner initializeCategories() {
        return args -> {
            if (repository.count() == 0) {
                repository.saveAll(Arrays.asList(
                        new Category("개발/프로그래밍"),
                        new Category("게임 개발"),
                        new Category("데이터 사이언스"),
                        new Category("인공지능"),
                        new Category("보안/네트워크"),
                        new Category("하드웨어"),
                        new Category("디자인/아트"),
                        new Category("기획/경영/마케팅"),
                        new Category("업무 생산성"),
                        new Category("커리어/자기계발"),
                        new Category("대학 교육")
                ));
            }
        };
    }
 }
