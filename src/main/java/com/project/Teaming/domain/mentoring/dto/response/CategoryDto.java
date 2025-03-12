package com.project.Teaming.domain.mentoring.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoryDto {
    private Long teamId;
    private String categoryId;

    public CategoryDto(Long teamId, String categoryId) {
        this.teamId = teamId;
        this.categoryId = categoryId;
    }
}
