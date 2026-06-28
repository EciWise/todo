package com.eciwise.todo.task.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subtasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Subtask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Builder.Default
    private boolean done = false;

    @Column(nullable = false)
    @Builder.Default
    private int position = 0;
}
