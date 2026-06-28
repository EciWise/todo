package com.eciwise.todo.task.application.port.out;

import com.eciwise.todo.task.domain.model.Importance;
import com.eciwise.todo.task.domain.model.Task;
import com.eciwise.todo.task.domain.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskPort {
    Task save(Task task);
    Optional<Task> findByIdAndOwnerId(Long id, Long ownerId);
    List<Task> findByOwnerIdOrdered(Long ownerId);
    Page<Task> search(Long ownerId, LocalDate date, String title, Importance importance, TaskStatus status, Pageable pageable);
    void delete(Task task);
    long countByOwnerId(Long ownerId);
    long countByOwnerIdAndStatus(Long ownerId, TaskStatus status);
    long countPlannedByOwnerId(Long ownerId);
    List<Task> findOverdue(Long ownerId, LocalDate before, TaskStatus statusNot);
    List<Task> findByOwnerIdAndDate(Long ownerId, LocalDate date);
}
