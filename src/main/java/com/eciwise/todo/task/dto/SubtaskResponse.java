package com.eciwise.todo.task.dto;

public record SubtaskResponse(
        Long id,
        String title,
        boolean done,
        int position
) {
}
