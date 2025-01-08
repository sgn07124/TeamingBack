package com.project.Teaming.domain.project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JoinTeamDto {

    @NotNull
    private Long teamId;

    @NotNull(message = "모집 카테고리를 입력해주세요. ex) 백엔드")
    private String recruitCategory;
}
