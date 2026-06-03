package com.eciwise.todo.task;

import com.eciwise.todo.exception.ResourceNotFoundException;
import com.eciwise.todo.task.dto.TaskCategoryRequest;
import com.eciwise.todo.task.dto.TaskCategoryResponse;
import com.eciwise.todo.user.AppUser;
import com.eciwise.todo.user.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final TaskCategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;
    private final TaskMapper mapper;

    public CategoryService(TaskCategoryRepository categoryRepository,
                           CurrentUserService currentUserService,
                           TaskMapper mapper) {
        this.categoryRepository = categoryRepository;
        this.currentUserService = currentUserService;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<TaskCategoryResponse> listForUser() {
        AppUser user = currentUserService.getOrCreate();
        return categoryRepository.findByOwner_IdOrderByNameAsc(user.getId()).stream()
                .map(mapper::toCategoryResponse)
                .toList();
    }

    @Transactional
    public TaskCategoryResponse create(TaskCategoryRequest request) {
        AppUser user = currentUserService.getOrCreate();
        TaskCategory category = categoryRepository.save(TaskCategory.builder()
                .owner(user)
                .name(request.name())
                .color(request.color())
                .build());
        return mapper.toCategoryResponse(category);
    }

    @Transactional
    public TaskCategoryResponse update(Long id, TaskCategoryRequest request) {
        AppUser user = currentUserService.getOrCreate();
        TaskCategory category = requireOwned(id, user);
        category.setName(request.name());
        category.setColor(request.color());
        return mapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        AppUser user = currentUserService.getOrCreate();
        categoryRepository.delete(requireOwned(id, user));
    }

    /** Resuelve una categoria propia del usuario o lanza 404 (usado por TaskService). */
    TaskCategory requireOwned(Long id, AppUser user) {
        return categoryRepository.findByIdAndOwner_Id(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada: " + id));
    }
}
