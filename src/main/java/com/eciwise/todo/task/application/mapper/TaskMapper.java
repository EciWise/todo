package com.eciwise.todo.task.application.mapper;

import com.eciwise.todo.task.application.dto.*;
import com.eciwise.todo.task.domain.model.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskMapper {

    public TaskResponse toResponse(Task task) {
        List<SubtaskResponse> subtasks = task.getSubtasks().stream()
                .map(this::toSubtaskResponse).toList();
        List<TagResponse> tags = task.getTags().stream()
                .map(this::toTagResponse).toList();
        TaskCategory category = task.getCategory();
        return new TaskResponse(
                task.getId(), task.getTitle(), task.getDescription(), task.getNotes(),
                task.getStatus(), task.getImportance(),
                category != null ? category.getId() : null,
                category != null ? category.getName() : null,
                task.getScheduledDate(), task.getStartTime(), task.getEndTime(),
                task.getColor(), task.getDayOrder(),
                task.getRecurrenceFreq(), task.getRecurrenceInterval(),
                task.getRecurrenceEndType(), task.getRecurrenceEndDate(), task.getRecurrenceCount(),
                task.getCompletedAt(), subtasks, tags, task.getCreatedAt(), task.getUpdatedAt());
    }

    public SubtaskResponse toSubtaskResponse(Subtask subtask) {
        return new SubtaskResponse(subtask.getId(), subtask.getTitle(), subtask.isDone(), subtask.getPosition());
    }

    public TagResponse toTagResponse(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName());
    }

    public TaskCategoryResponse toCategoryResponse(TaskCategory category) {
        return new TaskCategoryResponse(category.getId(), category.getName(), category.getColor());
    }

    public AchievementResponse toAchievementResponse(Achievement achievement) {
        return new AchievementResponse(achievement.getId(), achievement.getType(),
                achievement.getMilestone(), achievement.getAwardedAt());
    }
}
