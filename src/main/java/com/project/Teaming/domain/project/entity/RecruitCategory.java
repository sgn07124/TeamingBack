package com.project.Teaming.domain.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "project_recruit_category")
@NoArgsConstructor
@AllArgsConstructor
public class RecruitCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recruit_category_id")
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "name")
    private ProjectCategory name;
    @OneToMany(mappedBy = "recruitCategory")
    private List<TeamRecruitCategory> teamRecruitCategories = new ArrayList<>();
}
