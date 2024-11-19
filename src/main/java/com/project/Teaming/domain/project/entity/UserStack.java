package com.project.Teaming.domain.project.entity;

import com.project.Teaming.domain.user.entity.Portfolio;
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
    @JoinColumn(name = "user_stack_id",referencedColumnName = "stack_id")
    private Stack stack;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_portfolio_id", referencedColumnName = "portfolio_id")
    private Portfolio portfolio;
}
