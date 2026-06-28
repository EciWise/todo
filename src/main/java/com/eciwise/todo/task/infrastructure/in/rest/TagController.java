package com.eciwise.todo.task.infrastructure.in.rest;

import com.eciwise.todo.task.application.dto.TagRequest;
import com.eciwise.todo.task.application.dto.TagResponse;
import com.eciwise.todo.task.application.port.in.TagUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagUseCase tagUseCase;

    public TagController(TagUseCase tagUseCase) { this.tagUseCase = tagUseCase; }

    @GetMapping
    public List<TagResponse> findAll() { return tagUseCase.listForUser(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagResponse create(@Valid @RequestBody TagRequest request) { return tagUseCase.create(request); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { tagUseCase.delete(id); }
}
