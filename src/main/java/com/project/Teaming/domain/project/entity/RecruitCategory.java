package com.project.Teaming.domain.project.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
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
    @Tsid
    @Column(name = "recruit_category_id")
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "recruitCategory")
    private List<TeamRecruitCategory> teamRecruitCategories = new ArrayList<>();

    public RecruitCategory(String name) {
        this.name = name;
    }
}
