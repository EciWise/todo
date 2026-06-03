package com.eciwise.todo.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    List<Task> findByOwner_IdOrderByScheduledDateAscStartTimeAscDayOrderAsc(Long ownerId);

    Optional<Task> findByIdAndOwner_Id(Long id, Long ownerId);

    long countByOwner_IdAndStatus(Long ownerId, TaskStatus status);

    long countByOwner_Id(Long ownerId);

    // Numero de tareas que han llegado a estar planificadas (para los logros de planificacion).
    long countByOwner_IdAndPlannedNotifiedTrue(Long ownerId);

    // Tareas con fecha en un rango (para la agenda / expansion de recurrencia).
    List<Task> findByOwner_IdAndScheduledDateBetween(Long ownerId, LocalDate from, LocalDate to);

    // Tareas recurrentes del usuario (recurrenceFreq != NONE).
    List<Task> findByOwner_IdAndRecurrenceFreqNot(Long ownerId, RecurrenceFreq freq);

    // Vencidas: con fecha anterior a 'today' y no completadas.
    List<Task> findByOwner_IdAndScheduledDateBeforeAndStatusNot(
            Long ownerId, LocalDate today, TaskStatus status);

    // Tareas planificadas de hoy.
    List<Task> findByOwner_IdAndScheduledDate(Long ownerId, LocalDate date);
}
