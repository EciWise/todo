package com.eciwise.todo.task.application.dto;

import com.eciwise.todo.task.domain.model.Importance;
import com.eciwise.todo.task.domain.model.RecurrenceEndType;
import com.eciwise.todo.task.domain.model.RecurrenceFreq;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
) {}
