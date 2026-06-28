package com.eciwise.todo.task.application.port.in;

import com.eciwise.todo.task.application.dto.TagRequest;
import com.eciwise.todo.task.application.dto.TagResponse;

import java.util.List;

public interface TagUseCase {
    List<TagResponse> listForUser();
    TagResponse create(TagRequest request);
    void delete(Long id);
}
