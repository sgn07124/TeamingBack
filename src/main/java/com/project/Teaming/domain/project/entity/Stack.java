package com.project.Teaming.domain.project.entity;

import com.project.Teaming.domain.user.entity.UserStack;
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

    @Column(name = "stack_name",unique = true)
    private String stackName;
    @OneToMany(mappedBy = "stack")
    private List<TeamStack> teamStacks = new ArrayList<>();
    @OneToMany(mappedBy = "stack")
    private List<UserStack> userStacks = new ArrayList<>();

    public Stack(String stackName) {
        this.stackName = stackName;
    }
}
