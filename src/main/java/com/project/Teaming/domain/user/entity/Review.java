package com.project.Teaming.domain.user.entity;

import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.project.entity.ProjectParticipation;
import com.project.Teaming.domain.project.entity.ProjectTeam;
import com.project.Teaming.global.auditing.BaseEntity;
import com.project.Teaming.global.auditing.BaseTimeEntity;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "review")
@NoArgsConstructor
@AllArgsConstructor
public class   Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;  // 리뷰 ID

    @Column(name = "rating")
    private int rating;  // 별점(1~5)

    @Column(name = "review_text", columnDefinition = "TEXT")
    private String content;  // 리뷰 내용
    // 외래키 : 프로젝트id, 리뷰 대상자 id, 리뷰 작성자 id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_participation_id", referencedColumnName = "par_id",nullable = true)
    private ProjectParticipation projectParticipation;  // 프로젝트 유저 ID (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentoring_participation_id", referencedColumnName = "mp_id",nullable = true)
    private MentoringParticipation mentoringParticipation;  // 멘토링 유저 ID (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_id", referencedColumnName = "user_id",nullable = false)
    private User reviewee;  // 리뷰 대상자 ID

    public static Review projectReview(ProjectParticipation reviewerParticipation, User reviewee, int rating, String content) {
        Review review = new Review();
        review.projectParticipation = reviewerParticipation;
        review.reviewee = reviewee;
        review.rating = rating;
        review.content = content;
        return review;
    }

    public static Review mentoringReview(MentoringParticipation reviewerParticipation, User reviewee, int rating, String content) {
        Review review = new Review();
        review.mentoringParticipation = reviewerParticipation;
        review.rating = rating;
        review.content = content;
        review.reviewee = reviewee;
        if (!reviewee.getReviews().contains(review)) {
            reviewee.getReviews().add(review);
        }
        return review;
    }

    @PrePersist
    @PreUpdate
    private void validate() {
        // 멘토링 참여자와 프로젝트 참여자 중 하나만 존재해야 함
        if ((mentoringParticipation == null && projectParticipation == null) ||
                (mentoringParticipation != null && projectParticipation != null)) {
            throw new BusinessException(ErrorCode.CHOOSE_ONE_DOMAIN);
        }
    }
}