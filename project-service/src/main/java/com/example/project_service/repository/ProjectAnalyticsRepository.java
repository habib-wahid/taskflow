package com.example.project_service.repository;

import com.example.project_service.entity.ProjectAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectAnalyticsRepository extends JpaRepository<ProjectAnalytics, UUID> {

    Optional<ProjectAnalytics> findByProjectId(UUID projectId);
}
