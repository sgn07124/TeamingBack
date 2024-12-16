package com.project.Teaming.domain.mentoring.dto.response;

import com.project.Teaming.domain.mentoring.entity.MentoringAuthority;
import com.project.Teaming.domain.mentoring.entity.MentoringStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MyTeamDto {

    private Long id;
    private String name;  // 멘토링 명
    private LocalDate startDate;  // 멘토링 시작일
    private LocalDate endDate;  // 멘토링 종료일
    private MentoringStatus status;  //멘토링 상태
    private MentoringAuthority authority;  //사용자의 권한


    public MyTeamDto(Long id, String name, LocalDate startDate, LocalDate endDate, MentoringStatus status) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }
}
