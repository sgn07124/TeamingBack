package com.project.Teaming.domain.user.entity;

import com.project.Teaming.global.auditing.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "Portfolio")
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolioId")
    private Long id;  // 포트폴리오 ID
    @Column(name = "introduce", columnDefinition = "TEXT")
    private int introduce;  // 사용자 자기소개
    @Column(name = "techSkill")
    private String skills;  // 개발 스택 (여러 개 선택하는 경우, 테이블 분리 필요 여부 고려 예정)
    // 외래키 : 사용자 id
    @OneToOne(mappedBy = "portfolio")
    private User user;
}