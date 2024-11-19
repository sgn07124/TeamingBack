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
    @JoinColumn(name = "recruit_category_id")
    private RecruitCategory recruitCategory;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_team_id",referencedColumnName = "project_id")
    private ProjectTeam projectTeam;
}
