package com.eciwise.todo.task.domain.model;

import com.eciwise.todo.user.domain.model.AppUser;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "task_completion_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaskCompletionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Importance importance;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;
}
