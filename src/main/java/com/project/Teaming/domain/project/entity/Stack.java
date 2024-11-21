package com.project.Teaming.domain.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "project_stack")
@NoArgsConstructor
@AllArgsConstructor
public class Stack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stack_id")
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "stack_name",unique = true)
    private ProjectStack stackName;
    @OneToMany(mappedBy = "stack")
    private List<TeamStack> teamStacks = new ArrayList<>();
    @OneToMany(mappedBy = "stack")
    private List<UserStack> userStacks = new ArrayList<>();
}
