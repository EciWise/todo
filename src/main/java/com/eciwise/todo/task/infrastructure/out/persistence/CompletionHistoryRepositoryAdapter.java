package com.eciwise.todo.task.infrastructure.out.persistence;

import com.eciwise.todo.task.application.port.out.CompletionHistoryPort;
import com.eciwise.todo.task.domain.model.TaskCompletionHistory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class CompletionHistoryRepositoryAdapter implements CompletionHistoryPort {

    private final CompletionHistoryJpaRepository repository;

    public CompletionHistoryRepositoryAdapter(CompletionHistoryJpaRepository repository) { this.repository = repository; }

    @Override public TaskCompletionHistory save(TaskCompletionHistory h) { return repository.save(h); }
    @Override public long countByOwnerId(Long ownerId) { return repository.countByOwner_Id(ownerId); }
    @Override public List<TaskCompletionHistory> findByOwnerIdBetween(Long ownerId, Instant from, Instant to) { return repository.findByOwner_IdAndCompletedAtBetween(ownerId, from, to); }
    @Override public List<TaskCompletionHistory> findByOwnerIdOrderedByDate(Long ownerId) { return repository.findByOwner_IdOrderByCompletedAtDesc(ownerId); }
}
