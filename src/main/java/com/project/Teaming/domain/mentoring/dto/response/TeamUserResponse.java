package com.project.Teaming.domain.mentoring.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringRole;
import com.project.Teaming.domain.user.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
public class TeamUserResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acceptedTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;
    private String username;
    private MentoringRole role;
    private MentoringParticipationStatus status;
    private Boolean isLogined;
    private Boolean isDeleted;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isReported;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isReviewed;

    public TeamUserResponse(LocalDateTime acceptedTime, Long userId, String username, MentoringRole role, MentoringParticipationStatus status, Boolean isReviewed) {
        this.acceptedTime = acceptedTime;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.status = status;
        this.isLogined = false;
        this.isDeleted = false;
        this.isReviewed = (isReviewed != null && isReviewed) ? isReviewed : null;
    }

    public static TeamUserResponse toDto(MentoringParticipation mentoringParticipation, User user) {
        TeamUserResponse teamUserResponse = new TeamUserResponse();
        teamUserResponse.setAcceptedTime(mentoringParticipation.getDecisionDate());
        teamUserResponse.setUserId(user.getId());
        teamUserResponse.setUsername(user.getName());
        teamUserResponse.setRole(mentoringParticipation.getRole());
        teamUserResponse.setStatus(mentoringParticipation.getParticipationStatus());
        teamUserResponse.setIsLogined(false);
        teamUserResponse.setIsDeleted(true);
        teamUserResponse.setIsReported(false);
        teamUserResponse.setIsReviewed(false);
        return teamUserResponse;
    }
    public static List<TeamUserResponse> combine(List<TeamUserResponse> teamUsers, List<TeamUserResponse> deletedUsers) {
        return Stream.concat(teamUsers.stream(), deletedUsers.stream())
                .sorted(Comparator.comparing(TeamUserResponse::getAcceptedTime))
                .collect(Collectors.toList());
    }
}
