package com.eciwise.todo.task.infrastructure.out.persistence;

import com.eciwise.todo.task.application.port.out.TaskPort;
import com.eciwise.todo.task.domain.model.Importance;
import com.eciwise.todo.task.domain.model.Task;
import com.eciwise.todo.task.domain.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class TaskRepositoryAdapter implements TaskPort {

    private final TaskJpaRepository repository;

    public TaskRepositoryAdapter(TaskJpaRepository repository) {
        this.repository = repository;
    }

    @Override public Task save(Task task) { return repository.save(task); }

    @Override public Optional<Task> findByIdAndOwnerId(Long id, Long ownerId) {
        return repository.findByIdAndOwner_Id(id, ownerId);
    }

    @Override public List<Task> findByOwnerIdOrdered(Long ownerId) {
        return repository.findByOwner_IdOrderByScheduledDateAscStartTimeAscDayOrderAsc(ownerId);
    }

    @Override public Page<Task> search(Long ownerId, LocalDate date, String title,
                                       Importance importance, TaskStatus status, Pageable pageable) {
        Specification<Task> spec = Specification.where(TaskSpecifications.ownedBy(ownerId))
                .and(TaskSpecifications.onDate(date))
                .and(TaskSpecifications.titleContains(title))
                .and(TaskSpecifications.hasImportance(importance))
                .and(TaskSpecifications.hasStatus(status));
        return repository.findAll(spec, pageable);
    }

    @Override public void delete(Task task) { repository.delete(task); }
    @Override public long countByOwnerId(Long ownerId) { return repository.countByOwner_Id(ownerId); }
    @Override public long countByOwnerIdAndStatus(Long ownerId, TaskStatus status) { return repository.countByOwner_IdAndStatus(ownerId, status); }
    @Override public long countPlannedByOwnerId(Long ownerId) { return repository.countByOwner_IdAndPlannedNotifiedTrue(ownerId); }

    @Override public List<Task> findOverdue(Long ownerId, LocalDate before, TaskStatus statusNot) {
        return repository.findByOwner_IdAndScheduledDateBeforeAndStatusNot(ownerId, before, statusNot);
    }

    @Override public List<Task> findByOwnerIdAndDate(Long ownerId, LocalDate date) {
        return repository.findByOwner_IdAndScheduledDate(ownerId, date);
    }
}
