package com.eciwise.todo.task;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * Specifications dinamicas para la busqueda de tareas por fecha, titulo,
 * importancia y estado, siempre acotadas al dueño.
 */
public final class TaskSpecifications {

    private TaskSpecifications() {
    }

    public static Specification<Task> ownedBy(Long ownerId) {
        return (root, query, cb) -> cb.equal(root.get("owner").get("id"), ownerId);
    }

    public static Specification<Task> titleContains(String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        String pattern = "%" + title.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), pattern);
    }

    public static Specification<Task> onDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("scheduledDate"), date);
    }

    public static Specification<Task> hasImportance(Importance importance) {
        if (importance == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("importance"), importance);
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
