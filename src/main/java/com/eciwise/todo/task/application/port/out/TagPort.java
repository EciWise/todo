package com.eciwise.todo.task.application.port.out;

import com.eciwise.todo.task.domain.model.Tag;

import java.util.List;
import java.util.Optional;

public interface TagPort {
    List<Tag> findByOwnerIdOrderedByName(Long ownerId);
    Optional<Tag> findByIdAndOwnerId(Long id, Long ownerId);
    Optional<Tag> findByOwnerIdAndName(Long ownerId, String name);
    Tag save(Tag tag);
    void delete(Tag tag);
}
