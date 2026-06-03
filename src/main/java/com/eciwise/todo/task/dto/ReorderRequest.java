package com.eciwise.todo.task.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Mueve/reordena una tarea al arrastrarla en la agenda: nueva fecha, hora y
 * posicion dentro de la franja.
 */
public record ReorderRequest(
        LocalDate scheduledDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer dayOrder
) {
}
