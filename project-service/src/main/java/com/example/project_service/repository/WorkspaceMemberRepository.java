package com.example.project_service.repository;

import com.example.project_service.entity.WorkspaceMember;
import com.example.project_service.enums.WorkspaceRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, UUID> {

    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.workspace.id = :workspaceId AND wm.userId = :userId")
    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(@Param("workspaceId") UUID workspaceId,
                                                          @Param("userId") UUID userId);

    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.workspace.id = :workspaceId")
    List<WorkspaceMember> findAllByWorkspaceId(@Param("workspaceId") UUID workspaceId);

    boolean existsByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);

    @Query("SELECT wm.role FROM WorkspaceMember wm WHERE wm.workspace.id = :workspaceId AND wm.userId = :userId")
    Optional<WorkspaceRole> findRoleByWorkspaceIdAndUserId(@Param("workspaceId") UUID workspaceId,
                                                            @Param("userId") UUID userId);

    void deleteByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);

    @Query("SELECT COUNT(wm) FROM WorkspaceMember wm WHERE wm.workspace.id = :workspaceId")
    int countByWorkspaceId(@Param("workspaceId") UUID workspaceId);
}
