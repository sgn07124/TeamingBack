package com.project.Teaming.domain.project.dto.response;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectTeamInfoDto {

    private String projectName;
    private String startDate;
    private String endDate;
    private String deadline;
    private int memberCnt;
    private String link;
    private String contents;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private Long projectId;
    private List<String> stacks;  // 기술 스택(이름으로)
    private List<String> recruitCategories;  // 모집 구분(이름으로)
}
