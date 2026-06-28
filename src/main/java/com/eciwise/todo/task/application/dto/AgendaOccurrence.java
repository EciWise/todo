package com.eciwise.todo.task.application.dto;

import java.time.LocalDate;

public record AgendaOccurrence(LocalDate date, boolean virtual, TaskResponse task) {}
