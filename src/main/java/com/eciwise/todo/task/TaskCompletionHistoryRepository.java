package com.eciwise.todo.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface TaskCompletionHistoryRepository extends JpaRepository<TaskCompletionHistory, Long> {

    long countByOwner_Id(Long ownerId);

    List<TaskCompletionHistory> findByOwner_IdAndCompletedAtBetween(Long ownerId, Instant from, Instant to);

    List<TaskCompletionHistory> findByOwner_IdOrderByCompletedAtDesc(Long ownerId);
}
