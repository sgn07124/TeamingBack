package com.project.Teaming.domain.project.dto.response;

import com.project.Teaming.domain.project.entity.PostStatus;
import com.project.Teaming.domain.project.entity.ProjectBoard;
import com.project.Teaming.domain.project.entity.ProjectTeam;
import java.util.List;
import lombok.Data;

@Data
public class ProjectPostStatusDto {

    private PostStatus postStatus;

    public static ProjectPostStatusDto from(ProjectBoard projectBoard) {
        ProjectPostStatusDto dto = new ProjectPostStatusDto();
        dto.setPostStatus(projectBoard.getStatus());
        return dto;
    }
}
