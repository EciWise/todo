package com.eciwise.todo.task.dto;

import com.eciwise.todo.task.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(
        @NotNull TaskStatus status
) {
}
