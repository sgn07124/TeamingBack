package com.project.Teaming.domain.mentoring.dto.response;

import lombok.Data;

@Data
public class CategoryDto {
    private Long teamId;
    private String categoryId;

    public CategoryDto(Long teamId, String categoryId) {
        this.teamId = teamId;
        this.categoryId = categoryId;
    }
}
