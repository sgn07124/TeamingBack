package com.project.Teaming.domain.mentoring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "team_category")
@NoArgsConstructor
@AllArgsConstructor
public class TeamCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_category_id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id",nullable = false)
    private Category category;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentoring_team_id",nullable = false)
    private MentoringTeam mentoringTeam;

    public void setCategory(Category category) {
        this.category = category;
        category.getCategories().add(this);
    }

    public void removeCategory(Category category) {
        if (this.category != null) {
            this.category.getCategories().remove(this);
            this.category = null;
        }
    }

    public void setMentoringTeam(MentoringTeam mentoringTeam) {
        this.mentoringTeam = mentoringTeam;
        mentoringTeam.getCategories().add(this);
    }

    public void removeMentoringTeam(MentoringTeam mentoringTeam) {
        if (this.mentoringTeam != null) {
            this.mentoringTeam.getCategories().remove(this);
            this.mentoringTeam = null;
        }
    }

}
