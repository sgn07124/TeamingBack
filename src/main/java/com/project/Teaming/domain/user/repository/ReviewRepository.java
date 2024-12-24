package com.project.Teaming.domain.user.repository;

import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.project.entity.ProjectParticipation;
import com.project.Teaming.domain.user.dto.response.ReviewDto;
import com.project.Teaming.domain.user.entity.Review;
import com.project.Teaming.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByProjectParticipationAndReviewee(ProjectParticipation reviewer, User reviewee);
    boolean existsByMentoringParticipationAndReviewee(MentoringParticipation reviewer, User reviewee);

    @Query("select new com.project.Teaming.domain.user.dto.response.ReviewDto(ru.id, ru.name, r.content, r.createdDate) " +
            "from Review r " +
            "join r.reviewee ru " +
            "where ru = :user")
    List<ReviewDto> findAllByUser(@Param("user") User user);
}
