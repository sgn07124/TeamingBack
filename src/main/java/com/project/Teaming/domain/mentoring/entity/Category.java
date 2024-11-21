package com.project.Teaming.domain.mentoring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "category")
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "category_name",unique = true)
    private MentoringCategory name;
    @OneToMany(mappedBy = "category")
    private List<TeamCategory> categories = new ArrayList<>();
}
