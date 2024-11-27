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
    @Column(name = "category_name",unique = true)
    private String name;
    @OneToMany(mappedBy = "category")
    private List<TeamCategory> categories = new ArrayList<>();

    public Category(String name) {
        this.name = name;
    }
}
