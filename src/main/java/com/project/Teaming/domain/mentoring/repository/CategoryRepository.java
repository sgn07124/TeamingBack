package com.project.Teaming.domain.mentoring.repository;

import com.project.Teaming.domain.mentoring.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,Long> {
}
