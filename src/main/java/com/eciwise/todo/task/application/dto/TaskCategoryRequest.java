package com.eciwise.todo.task.application.dto;

import jakarta.validation.constraints.NotBlank;

public record TaskCategoryRequest(@NotBlank String name, String color) {}
