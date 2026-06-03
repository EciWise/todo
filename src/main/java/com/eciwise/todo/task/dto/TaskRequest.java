package com.eciwise.todo.task.dto;

import com.eciwise.todo.task.Importance;
import com.eciwise.todo.task.RecurrenceEndType;
import com.eciwise.todo.task.RecurrenceFreq;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Datos para crear o actualizar una tarea. Casi todo es opcional salvo el titulo.
 */
public record TaskRequest(
        @NotBlank String title,
        String description,
        String notes,
        Importance importance,
        Long categoryId,
        LocalDate scheduledDate,
        LocalTime startTime,
        LocalTime endTime,
        String color,
        Integer dayOrder,
        RecurrenceFreq recurrenceFreq,
        Integer recurrenceInterval,
        RecurrenceEndType recurrenceEndType,
        LocalDate recurrenceEndDate,
        Integer recurrenceCount,
        List<@NotBlank String> tags,
        List<SubtaskRequest> subtasks
) {
}
