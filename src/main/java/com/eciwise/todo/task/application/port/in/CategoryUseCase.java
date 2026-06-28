package com.eciwise.todo.task.application.port.in;

import com.eciwise.todo.task.application.dto.TaskCategoryRequest;
import com.eciwise.todo.task.application.dto.TaskCategoryResponse;

import java.util.List;

public interface CategoryUseCase {
    List<TaskCategoryResponse> listForUser();
    TaskCategoryResponse create(TaskCategoryRequest request);
    TaskCategoryResponse update(Long id, TaskCategoryRequest request);
    void delete(Long id);
}
