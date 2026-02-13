package com.example.project_service.repository;

import com.example.project_service.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

    Optional<Workspace> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("SELECT w FROM Workspace w JOIN w.members m WHERE m.userId = :userId AND w.isActive = true")
    List<Workspace> findAllByMemberUserId(@Param("userId") UUID userId);

    @Query("SELECT w FROM Workspace w WHERE w.id = :id AND w.isActive = true")
    Optional<Workspace> findByIdAndIsActiveTrue(@Param("id") UUID id);
}
