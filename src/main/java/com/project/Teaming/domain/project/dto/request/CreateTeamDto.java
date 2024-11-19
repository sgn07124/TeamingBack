package com.project.Teaming.domain.project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateTeamDto {

    @NotNull(message = "프로젝트 명을 입력해주세요.")
    private String projectName;

    @NotNull(message = "프로젝트 시작일을 입력해주세요.")
    private String startDate;

    @NotNull(message = "프로젝트 종료일을 입력해주세요.")
    private String endDate;

    @NotNull(message = "모집 인원을 입력해주세요.")
    private int memberCnt;

    private String link;

    @NotNull(message = "모집 소개를 입력해주세요.")
    private String contents;
}
