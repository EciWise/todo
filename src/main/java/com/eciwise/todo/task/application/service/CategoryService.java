package com.eciwise.todo.task.application.service;

import com.eciwise.todo.shared.exception.ResourceNotFoundException;
import com.eciwise.todo.task.application.dto.TaskCategoryRequest;
import com.eciwise.todo.task.application.dto.TaskCategoryResponse;
import com.eciwise.todo.task.application.mapper.TaskMapper;
import com.eciwise.todo.task.application.port.in.CategoryUseCase;
import com.eciwise.todo.task.application.port.out.CategoryPort;
import com.eciwise.todo.task.domain.model.TaskCategory;
import com.eciwise.todo.user.application.service.CurrentUserService;
import com.eciwise.todo.user.domain.model.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService implements CategoryUseCase {

    private final CategoryPort categoryPort;
    private final CurrentUserService currentUserService;
    private final TaskMapper mapper;

    public CategoryService(CategoryPort categoryPort, CurrentUserService currentUserService, TaskMapper mapper) {
        this.categoryPort = categoryPort;
        this.currentUserService = currentUserService;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskCategoryResponse> listForUser() {
        AppUser user = currentUserService.getOrCreate();
        return categoryPort.findByOwnerIdOrderedByName(user.getId()).stream()
                .map(mapper::toCategoryResponse).toList();
    }

    @Override
    @Transactional
    public TaskCategoryResponse create(TaskCategoryRequest request) {
        AppUser user = currentUserService.getOrCreate();
        TaskCategory category = categoryPort.save(TaskCategory.builder()
                .owner(user).name(request.name()).color(request.color()).build());
        return mapper.toCategoryResponse(category);
    }

    @Override
    @Transactional
    public TaskCategoryResponse update(Long id, TaskCategoryRequest request) {
        AppUser user = currentUserService.getOrCreate();
        TaskCategory category = categoryPort.findByIdAndOwnerId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada: " + id));
        category.setName(request.name());
        category.setColor(request.color());
        return mapper.toCategoryResponse(categoryPort.save(category));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AppUser user = currentUserService.getOrCreate();
        TaskCategory category = categoryPort.findByIdAndOwnerId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada: " + id));
        categoryPort.delete(category);
    }
}
