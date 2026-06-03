package com.eciwise.todo.task.dto;

import com.eciwise.todo.task.Importance;

import java.util.Map;

/** Panel de estadisticas del usuario. */
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
) {
}
