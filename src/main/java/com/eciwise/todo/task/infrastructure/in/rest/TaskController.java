package com.eciwise.todo.task.infrastructure.in.rest;

import com.eciwise.todo.task.application.dto.*;
import com.eciwise.todo.task.application.port.in.TaskUseCase;
import com.eciwise.todo.task.domain.model.Importance;
import com.eciwise.todo.task.domain.model.TaskStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskUseCase taskUseCase;

    public TaskController(TaskUseCase taskUseCase) {
        this.taskUseCase = taskUseCase;
    }

    @GetMapping
    public List<TaskResponse> findAll() { return taskUseCase.listForUser(); }

    @GetMapping("/{id}")
    public TaskResponse findById(@PathVariable Long id) { return taskUseCase.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskMutationResponse create(@Valid @RequestBody TaskRequest request) { return taskUseCase.create(request); }

    @PutMapping("/{id}")
    public TaskMutationResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) { return taskUseCase.update(id, request); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { taskUseCase.delete(id); }

    @PatchMapping("/{id}/status")
    public TaskMutationResponse changeStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return taskUseCase.changeStatus(id, request.status());
    }

    @PutMapping("/{id}/schedule")
    public TaskResponse reschedule(@PathVariable Long id, @RequestBody ReorderRequest request) { return taskUseCase.reschedule(id, request); }

    @GetMapping("/search")
    public Page<TaskResponse> search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Importance importance,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return taskUseCase.search(date, title, importance, status, pageable);
    }

    @GetMapping("/agenda")
    public List<AgendaOccurrence> agenda(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return taskUseCase.getAgenda(from, to);
    }

    @GetMapping("/overdue")
    public List<TaskResponse> overdue() { return taskUseCase.overdue(); }

    @GetMapping("/today")
    public List<TaskResponse> today() { return taskUseCase.today(); }

    @PostMapping("/{id}/subtasks")
    @ResponseStatus(HttpStatus.CREATED)
    public SubtaskResponse addSubtask(@PathVariable Long id, @Valid @RequestBody SubtaskRequest request) { return taskUseCase.addSubtask(id, request); }

    @PutMapping("/{id}/subtasks/{subtaskId}")
    public SubtaskResponse updateSubtask(@PathVariable Long id, @PathVariable Long subtaskId, @Valid @RequestBody SubtaskRequest request) { return taskUseCase.updateSubtask(id, subtaskId, request); }

    @DeleteMapping("/{id}/subtasks/{subtaskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubtask(@PathVariable Long id, @PathVariable Long subtaskId) { taskUseCase.deleteSubtask(id, subtaskId); }
}
