package com.example.project_service.repository;

import com.example.project_service.entity.ProjectMember;
import com.example.project_service.enums.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.userId = :userId")
    Optional<ProjectMember> findByProjectIdAndUserId(@Param("projectId") UUID projectId,
                                                      @Param("userId") UUID userId);

    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId")
    List<ProjectMember> findAllByProjectId(@Param("projectId") UUID projectId);

    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);

    @Query("SELECT pm.role FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.userId = :userId")
    Optional<ProjectRole> findRoleByProjectIdAndUserId(@Param("projectId") UUID projectId,
                                                        @Param("userId") UUID userId);

    void deleteByProjectIdAndUserId(UUID projectId, UUID userId);

    @Query("SELECT COUNT(pm) FROM ProjectMember pm WHERE pm.project.id = :projectId")
    int countByProjectId(@Param("projectId") UUID projectId);
}
