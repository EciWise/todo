package com.eciwise.todo.task.infrastructure.out.persistence;

import com.eciwise.todo.task.application.port.out.CategoryPort;
import com.eciwise.todo.task.domain.model.TaskCategory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CategoryRepositoryAdapter implements CategoryPort {

    private final CategoryJpaRepository repository;

    public CategoryRepositoryAdapter(CategoryJpaRepository repository) { this.repository = repository; }

    @Override public List<TaskCategory> findByOwnerIdOrderedByName(Long ownerId) { return repository.findByOwner_IdOrderByNameAsc(ownerId); }
    @Override public Optional<TaskCategory> findByIdAndOwnerId(Long id, Long ownerId) { return repository.findByIdAndOwner_Id(id, ownerId); }
    @Override public TaskCategory save(TaskCategory category) { return repository.save(category); }
    @Override public void delete(TaskCategory category) { repository.delete(category); }
}
