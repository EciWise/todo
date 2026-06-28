package com.eciwise.todo.task.application.port.out;

import com.eciwise.todo.task.domain.model.Achievement;

import java.util.List;

public interface AchievementPort {
    Achievement save(Achievement achievement);
    List<Achievement> findByOwnerIdOrderedByDate(Long ownerId);
}
