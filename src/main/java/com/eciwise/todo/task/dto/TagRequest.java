package com.eciwise.todo.task.dto;

import jakarta.validation.constraints.NotBlank;

public record TagRequest(
        @NotBlank String name
) {
}
