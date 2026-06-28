package com.eciwise.todo.task.infrastructure.out.persistence;

import com.eciwise.todo.task.domain.model.Subtask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubtaskJpaRepository extends JpaRepository<Subtask, Long> {
    Optional<Subtask> findByIdAndTask_Id(Long id, Long taskId);
}
