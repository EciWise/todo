package com.eciwise.todo.task;

import com.eciwise.todo.exception.ResourceNotFoundException;
import com.eciwise.todo.task.dto.TagRequest;
import com.eciwise.todo.task.dto.TagResponse;
import com.eciwise.todo.user.AppUser;
import com.eciwise.todo.user.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final CurrentUserService currentUserService;
    private final TaskMapper mapper;

    public TagService(TagRepository tagRepository,
                      CurrentUserService currentUserService,
                      TaskMapper mapper) {
        this.tagRepository = tagRepository;
        this.currentUserService = currentUserService;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<TagResponse> listForUser() {
        AppUser user = currentUserService.getOrCreate();
        return tagRepository.findByOwner_IdOrderByNameAsc(user.getId()).stream()
                .map(mapper::toTagResponse)
                .toList();
    }

    @Transactional
    public TagResponse create(TagRequest request) {
        AppUser user = currentUserService.getOrCreate();
        Tag tag = getOrCreate(user, request.name());
        return mapper.toTagResponse(tag);
    }

    @Transactional
    public void delete(Long id) {
        AppUser user = currentUserService.getOrCreate();
        Tag tag = tagRepository.findByIdAndOwner_Id(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta no encontrada: " + id));
        tagRepository.delete(tag);
    }

    /** Resuelve una etiqueta por nombre, creandola si no existe (usado por TaskService). */
    @Transactional
    public Tag getOrCreate(AppUser user, String name) {
        String trimmed = name.trim();
        return tagRepository.findByOwner_IdAndName(user.getId(), trimmed)
                .orElseGet(() -> tagRepository.save(Tag.builder()
                        .owner(user)
                        .name(trimmed)
                        .build()));
    }
}
