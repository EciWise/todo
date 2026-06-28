package com.eciwise.todo.task.application.dto;

import jakarta.validation.constraints.NotBlank;

public record SubtaskRequest(@NotBlank String title, Boolean done, Integer position) {}
