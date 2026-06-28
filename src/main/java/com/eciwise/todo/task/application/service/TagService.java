package com.eciwise.todo.task.application.service;

import com.eciwise.todo.shared.exception.ResourceNotFoundException;
import com.eciwise.todo.task.application.dto.TagRequest;
import com.eciwise.todo.task.application.dto.TagResponse;
import com.eciwise.todo.task.application.mapper.TaskMapper;
import com.eciwise.todo.task.application.port.in.TagUseCase;
import com.eciwise.todo.task.application.port.out.TagPort;
import com.eciwise.todo.task.domain.model.Tag;
import com.eciwise.todo.user.application.service.CurrentUserService;
import com.eciwise.todo.user.domain.model.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TagService implements TagUseCase {

    private final TagPort tagPort;
    private final CurrentUserService currentUserService;
    private final TaskMapper mapper;

    public TagService(TagPort tagPort, CurrentUserService currentUserService, TaskMapper mapper) {
        this.tagPort = tagPort;
        this.currentUserService = currentUserService;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> listForUser() {
        AppUser user = currentUserService.getOrCreate();
        return tagPort.findByOwnerIdOrderedByName(user.getId()).stream()
                .map(mapper::toTagResponse).toList();
    }

    @Override
    @Transactional
    public TagResponse create(TagRequest request) {
        AppUser user = currentUserService.getOrCreate();
        String trimmed = request.name().trim();
        Tag tag = tagPort.findByOwnerIdAndName(user.getId(), trimmed)
                .orElseGet(() -> tagPort.save(Tag.builder().owner(user).name(trimmed).build()));
        return mapper.toTagResponse(tag);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AppUser user = currentUserService.getOrCreate();
        Tag tag = tagPort.findByIdAndOwnerId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta no encontrada: " + id));
        tagPort.delete(tag);
    }
}
