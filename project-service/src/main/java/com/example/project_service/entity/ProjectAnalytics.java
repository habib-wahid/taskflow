package com.example.project_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "project_analytics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    private Project project;

    @Column(name = "total_tasks")
    @Builder.Default
    private Integer totalTasks = 0;

    @Column(name = "completed_tasks")
    @Builder.Default
    private Integer completedTasks = 0;

    @Column(name = "in_progress_tasks")
    @Builder.Default
    private Integer inProgressTasks = 0;

    @Column(name = "pending_tasks")
    @Builder.Default
    private Integer pendingTasks = 0;

    @Column(name = "overdue_tasks")
    @Builder.Default
    private Integer overdueTasks = 0;

    @Column(name = "total_members")
    @Builder.Default
    private Integer totalMembers = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
