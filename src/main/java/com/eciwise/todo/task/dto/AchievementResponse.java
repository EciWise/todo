package com.eciwise.todo.task.dto;

import com.eciwise.todo.task.AchievementType;

import java.time.Instant;

public record AchievementResponse(
        Long id,
        AchievementType type,
        int milestone,
        Instant awardedAt
) {
}
