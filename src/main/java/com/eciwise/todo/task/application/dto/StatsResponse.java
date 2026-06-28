package com.eciwise.todo.task.application.dto;

import com.eciwise.todo.task.domain.model.Importance;

import java.util.Map;

public record StatsResponse(
        long totalTasks,
        long pending,
        long inProgress,
        long done,
        long completedThisWeek,
        long completedThisMonth,
        double completionRate,
        int currentStreakDays,
        long overdue,
        long dueToday,
        Map<Importance, Long> completedByImportance
) {}
