package com.project.Teaming.domain.project.component;

import com.project.Teaming.domain.project.entity.RecruitCategory;
import com.project.Teaming.domain.project.repository.RecruitCategoryRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RecruitCategoryLoader implements CommandLineRunner {

    private final RecruitCategoryRepository recruitCategoryRepository;

    public RecruitCategoryLoader(RecruitCategoryRepository recruitCategoryRepository) {
        this.recruitCategoryRepository = recruitCategoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        List<RecruitCategory> categories = Arrays.asList(
                new RecruitCategory("기획자"),
                new RecruitCategory("디자이너"),
                new RecruitCategory("프론트엔드"),
                new RecruitCategory("백엔드"),
                new RecruitCategory("IOS"),
                new RecruitCategory("안드로이드"),
                new RecruitCategory("데브옵스"),
                new RecruitCategory("PM"),
                new RecruitCategory("마케터")
        );

        if (recruitCategoryRepository.count() == 0) {
            recruitCategoryRepository.saveAll(categories);
        }
    }
}
