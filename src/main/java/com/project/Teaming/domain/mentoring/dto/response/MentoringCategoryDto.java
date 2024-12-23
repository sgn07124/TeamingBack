package com.project.Teaming.domain.mentoring.dto.response;

import lombok.Data;

@Data
public class MentoringCategoryDto {

    private Long id;
    private String name;

    public MentoringCategoryDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
