package com.project.Teaming.domain.project.dto.response;

import com.project.Teaming.domain.project.entity.PostStatus;
import com.project.Teaming.domain.project.entity.ProjectParticipation;
import com.project.Teaming.domain.project.entity.ProjectRole;
import com.project.Teaming.domain.project.entity.ProjectStatus;
import com.project.Teaming.domain.project.entity.ProjectTeam;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MyProjectListDto {

    private String teamName;

    private String startDate;
    private String endDate;

    private ProjectStatus status;
    private ProjectRole role;

    private Long projectTeamId;
    private String createdDate;

    public static MyProjectListDto from(ProjectTeam projectTeam, ProjectParticipation participation) {
        MyProjectListDto dto = new MyProjectListDto();
        dto.setTeamName(projectTeam.getName());
        dto.setStartDate(String.valueOf(projectTeam.getStartDate()));
        dto.setEndDate(String.valueOf(projectTeam.getEndDate()));
        dto.setStatus(projectTeam.getStatus());
        dto.setRole(participation.getRole());
        dto.setProjectTeamId(projectTeam.getId());
        dto.setCreatedDate(dto.getFormattedDate(projectTeam.getCreatedDate()));
        return dto;
    }

    public String getFormattedDate(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
}
