package com.eciwise.todo.task.application.port.out;

import com.eciwise.todo.task.domain.model.Subtask;

import java.util.Optional;

public interface SubtaskPort {
    Subtask save(Subtask subtask);
    Optional<Subtask> findByIdAndTaskId(Long id, Long taskId);
    void delete(Subtask subtask);
}
