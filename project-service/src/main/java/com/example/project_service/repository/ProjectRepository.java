package com.example.project_service.repository;

import com.example.project_service.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @Query("SELECT p FROM Project p JOIN p.members pm WHERE pm.userId = :userId AND p.isActive = true")
    List<Project> findAllByMemberUserId(@Param("userId") UUID userId);

    @Query("SELECT p FROM Project p JOIN p.members pm WHERE p.workspace.id = :workspaceId AND pm.userId = :userId AND p.isActive = true")
    List<Project> findAllByWorkspaceIdAndMemberUserId(@Param("workspaceId") UUID workspaceId,
                                                       @Param("userId") UUID userId);

    @Query("SELECT p FROM Project p WHERE p.id = :id AND p.isActive = true")
    Optional<Project> findByIdAndIsActiveTrue(@Param("id") UUID id);

    boolean existsByWorkspaceIdAndKey(UUID workspaceId, String key);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.workspace.id = :workspaceId AND p.isActive = true")
    int countByWorkspaceId(@Param("workspaceId") UUID workspaceId);
}
