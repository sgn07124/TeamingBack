package com.project.Teaming.domain.project.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.Teaming.domain.project.entity.ProjectTeam;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private String createdDate;
    private String lastModifiedDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;  // projectId

    private List<String> stacks;  // 기술 스택(id로)
    private List<String> recruitCategories;  // 모집 구분(id로)
    private String userRole;

    public static ProjectTeamInfoDto from(ProjectTeam projectTeam, List<String> stackIds, List<String> recruitCategoryIds, String userRole) {
        ProjectTeamInfoDto dto = new ProjectTeamInfoDto();
        dto.setId(projectTeam.getId());
        dto.setProjectName(projectTeam.getName());
        dto.setStartDate(String.valueOf(projectTeam.getStartDate()));
        dto.setEndDate(String.valueOf(projectTeam.getEndDate()));
        dto.setDeadline(String.valueOf(projectTeam.getDeadline()));
        dto.setMemberCnt(projectTeam.getMembersCnt());
        dto.setLink(projectTeam.getLink());
        dto.setContents(projectTeam.getContents());
        dto.setCreatedDate(dto.getFormattedDate(projectTeam.getCreatedDate()));
        dto.setLastModifiedDate(dto.getFormattedDate(projectTeam.getLastModifiedDate()));
        dto.setStacks(stackIds);
        dto.setRecruitCategories(recruitCategoryIds);
        dto.setUserRole(userRole);
        return dto;
    }

    public String getFormattedDate(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
}
