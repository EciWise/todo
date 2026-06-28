package com.eciwise.todo.task.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReorderRequest(LocalDate scheduledDate, LocalTime startTime, LocalTime endTime, Integer dayOrder) {}
