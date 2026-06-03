package com.eciwise.todo.task.dto;

import com.eciwise.todo.task.Importance;
import com.eciwise.todo.task.RecurrenceEndType;
import com.eciwise.todo.task.RecurrenceFreq;
import com.eciwise.todo.task.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record TaskResponse(
        Long id,
        String title,
        String description,
        String notes,
        TaskStatus status,
        Importance importance,
        Long categoryId,
        String categoryName,
        LocalDate scheduledDate,
        LocalTime startTime,
        LocalTime endTime,
        String color,
        int dayOrder,
        RecurrenceFreq recurrenceFreq,
        int recurrenceInterval,
        RecurrenceEndType recurrenceEndType,
        LocalDate recurrenceEndDate,
        Integer recurrenceCount,
        Instant completedAt,
        List<SubtaskResponse> subtasks,
        List<TagResponse> tags,
        Instant createdAt,
        Instant updatedAt
) {
}
