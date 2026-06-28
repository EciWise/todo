package com.eciwise.todo.task.infrastructure.in.rest;

import com.eciwise.todo.task.application.dto.TaskCategoryRequest;
import com.eciwise.todo.task.application.dto.TaskCategoryResponse;
import com.eciwise.todo.task.application.port.in.CategoryUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryUseCase categoryUseCase;

    public CategoryController(CategoryUseCase categoryUseCase) { this.categoryUseCase = categoryUseCase; }

    @GetMapping
    public List<TaskCategoryResponse> findAll() { return categoryUseCase.listForUser(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskCategoryResponse create(@Valid @RequestBody TaskCategoryRequest request) { return categoryUseCase.create(request); }

    @PutMapping("/{id}")
    public TaskCategoryResponse update(@PathVariable Long id, @Valid @RequestBody TaskCategoryRequest request) { return categoryUseCase.update(id, request); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { categoryUseCase.delete(id); }
}
