package com.eciwise.todo.task.application.dto;

import jakarta.validation.constraints.NotBlank;

public record TagRequest(@NotBlank String name) {}
