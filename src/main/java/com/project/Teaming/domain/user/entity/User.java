package com.project.Teaming.domain.user.entity;

import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.project.entity.ProjectParticipation;
import com.project.Teaming.domain.user.dto.request.RegisterDto;
import com.project.Teaming.global.auditing.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "user")
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;  // 사용자Id

    @Column(name = "user_email", nullable = false, unique = true, length = 100)
    private String email;  // 사용자 이메일

    @Column(name = "user_name", length = 50)
    private String name;  // 사용자 이름

    @Column(name = "warning_count", nullable = false)
    private Integer warningCount;  // 사용자가 받은 경고 횟수

    @Column(name = "user_role")
    private String userRole;

    @Column(name = "provider")
    private String provider;

    @OneToMany(mappedBy = "user")
    private List<ProjectParticipation> projectParticipations = new ArrayList<>();

    @OneToMany(mappedBy = "reportedUser")
    private List<Report> reports = new ArrayList<>();

    @OneToMany(mappedBy = "reviewee", fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "portfolio_id", referencedColumnName = "portfolio_id", nullable = true)
    private Portfolio portfolio;

    @OneToMany(mappedBy = "user")
    private List<MentoringParticipation> mentoringParticipations = new ArrayList<>();

    public User(String email, String provider, String role) {
        this.email = email;
        this.provider = provider;
        this.userRole = role;
        this.warningCount = 0;  // 처음 경고 횟수는 0으로 설정
    }

    // 추가 정보 기입
    public void updateUserInfo(String name) {
        this.name = name;
    }

    /**
     * 포트폴리오 설정 비즈니스 로직
     */
    public void registerPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }
    public void linkPortfolio(Portfolio portfolio, RegisterDto dto) {
        portfolio.assignUser(this);
        portfolio.updatePortfolioInfo(dto.getIntroduce());
    }

    public void update(String name, Portfolio portfolio) {
        this.name = name;
        this.portfolio = portfolio;
    }

    public void incrementWarningCnt() {
        this.warningCount += 1;
    }
}
