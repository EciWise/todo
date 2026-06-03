package com.eciwise.todo.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubtaskRepository extends JpaRepository<Subtask, Long> {

    Optional<Subtask> findByIdAndTask_Id(Long id, Long taskId);
}
