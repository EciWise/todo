package com.eciwise.todo.task.application.dto;

import com.eciwise.todo.task.domain.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull TaskStatus status) {}
