package com.eciwise.todo.task.dto;

import jakarta.validation.constraints.NotBlank;

public record TaskCategoryRequest(
        @NotBlank String name,
        String color
) {
}
