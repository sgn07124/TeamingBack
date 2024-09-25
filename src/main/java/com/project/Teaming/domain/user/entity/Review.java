package com.project.Teaming.domain.user.entity;

import com.project.Teaming.domain.project.entity.ProjectTeam;
import com.project.Teaming.global.auditing.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "Review")
@NoArgsConstructor
@AllArgsConstructor
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reviewId")
    private Long id;  // 리뷰 ID

    @Column(name = "rating")
    private int rating;  // 별점(1~5)

    @Column(name = "reviewText", columnDefinition = "TEXT")
    private String content;  // 리뷰 내용

    // 외래키 : 프로젝트id, 리뷰 대상자 id, 리뷰 작성자 id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parId")
    private ProjectTeam projectTeam;  // 프로젝트 팀 ID (주인)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewerId", referencedColumnName = "userId")
    private User reviewer;  // 리뷰 작성자 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revieweeId", referencedColumnName = "userId")
    private User reviewee;  // 리뷰 대상자 ID
}
