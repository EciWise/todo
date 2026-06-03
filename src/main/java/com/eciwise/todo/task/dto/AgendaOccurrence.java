package com.eciwise.todo.task.dto;

import java.time.LocalDate;

/**
 * Ocurrencia (real o virtual) de una tarea en una fecha concreta de la agenda.
 * Para tareas recurrentes, 'date' puede diferir de task.scheduledDate y
 * 'virtual' sera true (no existe como fila propia).
 */
public record AgendaOccurrence(
        LocalDate date,
        boolean virtual,
        TaskResponse task
) {
}
