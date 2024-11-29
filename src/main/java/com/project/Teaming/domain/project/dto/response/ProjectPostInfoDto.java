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

    private Long projectTeamId;

    private List<String> stacks;  // 기술 스택(이름으로)
    private List<String> recruitCategories;  // 모집 구분(이름으로)

    public static ProjectPostInfoDto from(ProjectTeam projectTeam, ProjectBoard projectBoard, List<String> stackNames, List<String> recruitCategoryNames) {
        ProjectPostInfoDto dto = new ProjectPostInfoDto();
        dto.setTitle(projectBoard.getTitle());
        dto.setTeamName(projectTeam.getName());
        dto.setStartDate(projectTeam.getStartDate());
        dto.setEndDate(projectTeam.getEndDate());
        dto.setDeadline(projectBoard.getDeadline());
        dto.setMemberCnt(projectBoard.getMembersCnt());
        dto.setLink(projectBoard.getLink());
        dto.setContents(projectBoard.getContents());
        dto.setProjectTeamId(projectTeam.getId());
        dto.setStacks(stackNames);
        dto.setRecruitCategories(recruitCategoryNames);
        return dto;
    }
}
