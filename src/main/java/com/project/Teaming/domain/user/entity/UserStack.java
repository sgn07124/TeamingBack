package com.project.Teaming.domain.user.entity;

import com.project.Teaming.domain.project.entity.Stack;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "project_user_stack")
@NoArgsConstructor
@AllArgsConstructor
public class UserStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stack_id",nullable = false)
    private Stack stack;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id",nullable = false)
    private Portfolio portfolio;

    public UserStack(Stack stack, Portfolio portfolio) {
        this.stack = stack;
        this.portfolio = portfolio;
    }

    public static UserStack addStacks(Portfolio portfolio, Stack stack) {
        UserStack userStack = new UserStack();
        userStack.stack = stack;
        userStack.portfolio = portfolio;
        return userStack;
    }
}
