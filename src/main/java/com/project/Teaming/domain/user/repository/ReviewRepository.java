package com.project.Teaming.domain.user.repository;

import com.project.Teaming.domain.project.entity.ProjectParticipation;
import com.project.Teaming.domain.user.entity.Review;
import com.project.Teaming.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByProjectParticipationAndReviewee(ProjectParticipation reviewer, User reviewee);
}
