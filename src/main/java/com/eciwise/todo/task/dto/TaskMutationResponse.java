package com.eciwise.todo.task.dto;

import java.util.List;

/**
 * Respuesta a una mutacion de tarea: la tarea resultante mas las felicitaciones
 * recien otorgadas (para que el front muestre el toast). La lista va vacia si no
 * se alcanzo ningun hito.
 */
public record TaskMutationResponse(
        TaskResponse task,
        List<AchievementResponse> achievements
) {
}
