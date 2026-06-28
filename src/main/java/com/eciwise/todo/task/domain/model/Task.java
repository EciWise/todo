package com.eciwise.todo.task.domain.model;

import com.eciwise.todo.user.domain.model.AppUser;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EntityListeners(AuditingEntityListener.class)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "text")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Importance importance = Importance.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private TaskCategory category;

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(length = 16)
    private String color;

    @Column(name = "day_order", nullable = false)
    @Builder.Default
    private int dayOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_freq", nullable = false, length = 20)
    @Builder.Default
    private RecurrenceFreq recurrenceFreq = RecurrenceFreq.NONE;

    @Column(name = "recurrence_interval", nullable = false)
    @Builder.Default
    private int recurrenceInterval = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_end_type", nullable = false, length = 20)
    @Builder.Default
    private RecurrenceEndType recurrenceEndType = RecurrenceEndType.NEVER;

    @Column(name = "recurrence_end_date")
    private LocalDate recurrenceEndDate;

    @Column(name = "recurrence_count")
    private Integer recurrenceCount;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "planned_notified", nullable = false)
    @Builder.Default
    private boolean plannedNotified = false;

    @Column(name = "completed_notified", nullable = false)
    @Builder.Default
    private boolean completedNotified = false;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC, id ASC")
    @Builder.Default
    private List<Subtask> subtasks = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "task_tags",
               joinColumns = @JoinColumn(name = "task_id"),
               inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @Builder.Default
    private Set<Tag> tags = new LinkedHashSet<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
