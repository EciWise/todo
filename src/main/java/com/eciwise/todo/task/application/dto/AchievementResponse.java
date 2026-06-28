package com.eciwise.todo.task.application.dto;

import com.eciwise.todo.task.domain.model.AchievementType;

import java.time.Instant;

public record AchievementResponse(Long id, AchievementType type, int milestone, Instant awardedAt) {}
