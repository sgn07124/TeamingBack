package com.project.Teaming.domain.project.dto.response;

import com.project.Teaming.domain.project.entity.PostStatus;
import com.project.Teaming.domain.project.entity.ProjectBoard;
import com.project.Teaming.domain.project.entity.ProjectTeam;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.Data;

@Data
public class ProjectPostListDto {

    private String title;
    private String teamName;

    private String startDate;
    private String endDate;
    private String contents;
    private PostStatus status;

    private Long projectTeamId;
    private Long postId;
    private String createdDate;

    private List<Long> stacks;  // 기술 스택(id 조회)

    public static ProjectPostListDto from(ProjectTeam projectTeam, ProjectBoard projectBoard, List<Long> stackIds) {
        ProjectPostListDto dto = new ProjectPostListDto();
        dto.setTitle(projectBoard.getTitle());
        dto.setTeamName(projectTeam.getName());
        dto.setStartDate(projectTeam.getStartDate());
        dto.setEndDate(projectTeam.getEndDate());
        dto.setContents(projectBoard.getContents());
        dto.setStatus(projectBoard.getStatus());
        dto.setProjectTeamId(projectTeam.getId());
        dto.setPostId(projectBoard.getId());
        dto.setCreatedDate(dto.getFormattedDate(projectBoard.getCreatedDate()));
        dto.setStacks(stackIds);
        return dto;
    }

    public String getFormattedDate(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
}
