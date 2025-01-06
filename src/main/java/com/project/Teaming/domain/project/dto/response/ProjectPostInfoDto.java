package com.project.Teaming.domain.project.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.Teaming.domain.project.entity.ProjectBoard;
import com.project.Teaming.domain.project.entity.ProjectTeam;
import java.util.List;
import lombok.Data;

@Data
public class ProjectPostInfoDto {

    private String title;
    private String teamName;
    private Long teamId;

    private String startDate;
    private String endDate;
    private String deadline;
    private int memberCnt;
    private String link;
    private String contents;
    private String postStatus;
    @JsonProperty("isMember")
    private boolean isMember;
    @JsonProperty("isApply")
    private boolean isApply;

    private Long projectTeamId;
    private Long postId;

    private List<String> stacks;  // 기술 스택(id 조회)
    private List<String> recruitCategories;  // 모집 구분(id 조회)

    public static ProjectPostInfoDto from(ProjectTeam projectTeam, ProjectBoard projectBoard, List<String> stackIds, List<String> recruitCategoryIds, boolean isMember, boolean isApply) {
        ProjectPostInfoDto dto = new ProjectPostInfoDto();
        dto.setTitle(projectBoard.getTitle());
        dto.setTeamName(projectTeam.getName());
        dto.setTeamId(projectTeam.getId());
        dto.setStartDate(String.valueOf(projectTeam.getStartDate()));
        dto.setEndDate(String.valueOf(projectTeam.getEndDate()));
        dto.setDeadline(String.valueOf(projectBoard.getDeadline()));
        dto.setMemberCnt(projectBoard.getMembersCnt());
        dto.setLink(projectBoard.getLink());
        dto.setContents(projectBoard.getContents());
        dto.setPostStatus(projectBoard.getStatus().name());
        dto.setMember(isMember);
        dto.setApply(isApply);
        dto.setProjectTeamId(projectTeam.getId());
        dto.setPostId(projectBoard.getId());
        dto.setStacks(stackIds);
        dto.setRecruitCategories(recruitCategoryIds);
        return dto;
    }
}
