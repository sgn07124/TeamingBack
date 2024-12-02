package com.project.Teaming.domain.project.dto.response;

import com.project.Teaming.domain.project.entity.ProjectBoard;
import com.project.Teaming.domain.project.entity.ProjectTeam;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class ProjectPostInfoDto {

    private String title;
    private String teamName;

    private String startDate;
    private String endDate;
    private String deadline;
    private int memberCnt;
    private String link;
    private String contents;
    private String postStatus;

    private Long projectTeamId;
    private Long postId;

    private List<String> stacks;  // 기술 스택(id 조회)
    private List<String> recruitCategories;  // 모집 구분(id 조회)

    public static ProjectPostInfoDto from(ProjectTeam projectTeam, ProjectBoard projectBoard, List<String> stackIds, List<String> recruitCategoryIds) {
        ProjectPostInfoDto dto = new ProjectPostInfoDto();
        dto.setTitle(projectBoard.getTitle());
        dto.setTeamName(projectTeam.getName());
        dto.setStartDate(String.valueOf(projectTeam.getStartDate()));
        dto.setEndDate(String.valueOf(projectTeam.getEndDate()));
        dto.setDeadline(String.valueOf(projectBoard.getDeadline()));
        dto.setMemberCnt(projectBoard.getMembersCnt());
        dto.setLink(projectBoard.getLink());
        dto.setContents(projectBoard.getContents());
        dto.setPostStatus(projectBoard.getStatus().name());
        dto.setProjectTeamId(projectTeam.getId());
        dto.setPostId(projectBoard.getId());
        dto.setStacks(stackIds);
        dto.setRecruitCategories(recruitCategoryIds);
        return dto;
    }
}
