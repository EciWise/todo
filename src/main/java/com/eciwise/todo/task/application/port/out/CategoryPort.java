package com.eciwise.todo.task.application.port.out;

import com.eciwise.todo.task.domain.model.TaskCategory;

import java.util.List;
import java.util.Optional;

public interface CategoryPort {
    List<TaskCategory> findByOwnerIdOrderedByName(Long ownerId);
    Optional<TaskCategory> findByIdAndOwnerId(Long id, Long ownerId);
    TaskCategory save(TaskCategory category);
    void delete(TaskCategory category);
}
