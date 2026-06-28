package com.eciwise.todo.task.application.port.out;

import com.eciwise.todo.task.domain.model.TaskCompletionHistory;

import java.time.Instant;
import java.util.List;

public interface CompletionHistoryPort {
    TaskCompletionHistory save(TaskCompletionHistory history);
    long countByOwnerId(Long ownerId);
    List<TaskCompletionHistory> findByOwnerIdBetween(Long ownerId, Instant from, Instant to);
    List<TaskCompletionHistory> findByOwnerIdOrderedByDate(Long ownerId);
}
