package com.project.Teaming.domain.user.entity;

import com.project.Teaming.domain.project.entity.Stack;
import com.project.Teaming.domain.user.dto.request.UpdateUserStackDto;
import com.project.Teaming.global.auditing.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "portfolio")
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private Long id;  // 포트폴리오 ID

    @Column(name = "introduce", columnDefinition = "TEXT")
    private String introduce;  // 사용자 자기소개

    // 외래키 : 사용자 id
    @OneToOne(mappedBy = "portfolio")
    private User user;
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserStack> userStacks = new ArrayList<>();

    public void assignUser(User user) {
        this.user = user;
    }

    // 추가 정보 기입
    public void updatePortfolioInfo(String introduce) {
        this.introduce = introduce;
    }

    public void updateStacks(List<Stack> stacks) {
        // 기존 스택 제거
        this.userStacks.clear();

        // 새로 입력된 기술 스택 추가
        for (Stack stack : stacks) {
            UserStack userStack = new UserStack(stack, this);
            this.userStacks.add(userStack);
        }
    }
}