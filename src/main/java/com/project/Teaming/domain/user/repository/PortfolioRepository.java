package com.project.Teaming.domain.user.repository;

import com.project.Teaming.domain.user.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
}
