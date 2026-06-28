package com.eciwise.todo.task.application.dto;

public record SubtaskResponse(Long id, String title, boolean done, int position) {}
