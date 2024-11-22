package com.project.Teaming.domain.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "project_team_recruit_category")
@NoArgsConstructor
@AllArgsConstructor
public class TeamRecruitCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruit_category_id",nullable = false)
    private RecruitCategory recruitCategory;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id",nullable = false)
    private ProjectTeam projectTeam;

    public TeamRecruitCategory(RecruitCategory recruitCategory, ProjectTeam projectTeam) {
        this.recruitCategory = recruitCategory;
        this.projectTeam = projectTeam;
    }

    public static TeamRecruitCategory addRecruitCategories(ProjectTeam projectTeam, RecruitCategory recruitCategory) {
        TeamRecruitCategory teamRecruitCategory = new TeamRecruitCategory();
        teamRecruitCategory.projectTeam = projectTeam;
        teamRecruitCategory.recruitCategory = recruitCategory;
        return teamRecruitCategory;
    }
}
