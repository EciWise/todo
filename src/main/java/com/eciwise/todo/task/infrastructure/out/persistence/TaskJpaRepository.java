package com.eciwise.todo.task.infrastructure.out.persistence;

import com.eciwise.todo.task.domain.model.Task;
import com.eciwise.todo.task.domain.model.TaskStatus;
import com.eciwise.todo.task.domain.model.RecurrenceFreq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskJpaRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    List<Task> findByOwner_IdOrderByScheduledDateAscStartTimeAscDayOrderAsc(Long ownerId);
    Optional<Task> findByIdAndOwner_Id(Long id, Long ownerId);
    long countByOwner_IdAndStatus(Long ownerId, TaskStatus status);
    long countByOwner_Id(Long ownerId);
    long countByOwner_IdAndPlannedNotifiedTrue(Long ownerId);
    List<Task> findByOwner_IdAndScheduledDateBetween(Long ownerId, LocalDate from, LocalDate to);
    List<Task> findByOwner_IdAndRecurrenceFreqNot(Long ownerId, RecurrenceFreq freq);
    List<Task> findByOwner_IdAndScheduledDateBeforeAndStatusNot(Long ownerId, LocalDate today, TaskStatus status);
    List<Task> findByOwner_IdAndScheduledDate(Long ownerId, LocalDate date);
}
