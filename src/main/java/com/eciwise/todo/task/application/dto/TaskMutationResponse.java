package com.eciwise.todo.task.application.dto;

import java.util.List;

public record TaskMutationResponse(TaskResponse task, List<AchievementResponse> achievements) {}
