package com.project.Teaming.domain.project.dto.request;

import com.project.Teaming.domain.project.entity.ProjectStatus;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTeamDto {

    @NotBlank(message = "프로젝트 명을 입력해주세요.")
    private String projectName;

    @NotNull(message = "프로젝트 시작일을 입력해주세요.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String startDate;

    @NotNull(message = "프로젝트 종료일을 입력해주세요.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String endDate;

    @NotNull(message = "팀 모집 마감일을 입력해주세요.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String deadline;

    @NotNull(message = "모집 인원을 입력해주세요.")
    @Min(value = 1, message = "모집 인원은 최소 1명이어야 합니다.")
    @Max(value = 20, message = "모집 인원은 최대 20명까지 가능합니다.")
    @Digits(integer = 2, fraction = 0, message = "모집 인원은 정수만 입력 가능합니다.")
    private int memberCnt;

    private String link;

    @NotBlank(message = "모집 소개를 입력해주세요.")
    private String contents;

    @NotNull(message = "기술 스택을 선택해주세요.")
    private List<Long> stackIds; // 선택된 기술 스택의 ID 리스트

    @NotNull(message = "모집 구분을 선택해주세요.")
    private List<Long> recruitCategoryIds; // 선택된 기술 스택의 ID 리스트
}
