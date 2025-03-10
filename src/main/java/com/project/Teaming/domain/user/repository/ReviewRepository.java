package com.project.Teaming.domain.user.repository;

import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.project.entity.ProjectParticipation;
import com.project.Teaming.domain.user.dto.response.ReviewDto;
import com.project.Teaming.domain.user.entity.Review;
import com.project.Teaming.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("select r.reviewee.id from Review r where r.projectParticipation = :projectParticipation and r.reviewee.id in :userIds")
    Set<Long> findAllByProjectParticipationAndRevieweeIn(@Param("projectParticipation") ProjectParticipation projectParticipation,
                                                             @Param("userIds")List<Long> userIds);
    boolean existsByMentoringParticipationAndReviewee(MentoringParticipation reviewer, User reviewee);

    boolean existsByProjectParticipationAndRevieweeId(ProjectParticipation projectParticipation, Long revieweeId);

    @Query("select new com.project.Teaming.domain.user.dto.response.ReviewDto(mu.id, mu.name, r.content, r.createdDate,r.rating) " +
            "from Review r " +
            "join r.reviewee ru " +
            "join r.mentoringParticipation mp " +
            "join mp.user mu " +
            "where ru = :user")
    List<ReviewDto> findMentoringReviewsByUser(@Param("user") User user);

    @Query("select new com.project.Teaming.domain.user.dto.response.ReviewDto(pu.id, pu.name, r.content, r.createdDate, r.rating) " +
            "from Review r " +
            "join r.reviewee ru " +
            "left join r.projectParticipation pp " +
            "left join pp.user pu " +
            "where ru = :user")
    List<ReviewDto> findProjectReviewsByUser(@Param("user") User user);

    @Query("SELECT r.reviewee.id " +
            "FROM Review r " +
            "WHERE r.mentoringParticipation.id = :currentParticipationId " +
            "AND r.reviewee.id IN :userIds")
    Set<Long> findReviewedUserIds(@Param("currentParticipationId") Long currentParticipationId,
                                  @Param("userIds") Set<Long> userIds);

    @Modifying
    @Query("UPDATE Review r SET r.projectParticipation = NULL WHERE r.projectParticipation.id = :participationId")
    void updateProjectParticipationNull(@Param("participationId") Long participationId);

    @Modifying
    @Query("DELETE FROM Review r WHERE r.reviewee.id = :userId")
    void deleteByRevieweeId(@Param("userId") Long userId);
}
