package com.eciwise.todo.task.infrastructure.out.persistence;

import com.eciwise.todo.task.application.port.out.TagPort;
import com.eciwise.todo.task.domain.model.Tag;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TagRepositoryAdapter implements TagPort {

    private final TagJpaRepository repository;

    public TagRepositoryAdapter(TagJpaRepository repository) { this.repository = repository; }

    @Override public List<Tag> findByOwnerIdOrderedByName(Long ownerId) { return repository.findByOwner_IdOrderByNameAsc(ownerId); }
    @Override public Optional<Tag> findByIdAndOwnerId(Long id, Long ownerId) { return repository.findByIdAndOwner_Id(id, ownerId); }
    @Override public Optional<Tag> findByOwnerIdAndName(Long ownerId, String name) { return repository.findByOwner_IdAndName(ownerId, name); }
    @Override public Tag save(Tag tag) { return repository.save(tag); }
    @Override public void delete(Tag tag) { repository.delete(tag); }
}
