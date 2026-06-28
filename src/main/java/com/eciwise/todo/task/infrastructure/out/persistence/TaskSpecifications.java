package com.eciwise.todo.task.infrastructure.out.persistence;

import com.eciwise.todo.task.domain.model.Importance;
import com.eciwise.todo.task.domain.model.Task;
import com.eciwise.todo.task.domain.model.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class TaskSpecifications {

    private TaskSpecifications() {}

    public static Specification<Task> ownedBy(Long ownerId) {
        return (root, query, cb) -> cb.equal(root.get("owner").get("id"), ownerId);
    }

    public static Specification<Task> titleContains(String title) {
        if (title == null || title.isBlank()) return null;
        String pattern = "%" + title.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), pattern);
    }

    public static Specification<Task> onDate(LocalDate date) {
        if (date == null) return null;
        return (root, query, cb) -> cb.equal(root.get("scheduledDate"), date);
    }

    public static Specification<Task> hasImportance(Importance importance) {
        if (importance == null) return null;
        return (root, query, cb) -> cb.equal(root.get("importance"), importance);
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        if (status == null) return null;
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
