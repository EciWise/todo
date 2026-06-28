package com.eciwise.todo.task.application.port.in;

import com.eciwise.todo.task.application.dto.*;
import com.eciwise.todo.task.domain.model.Importance;
import com.eciwise.todo.task.domain.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface TaskUseCase {
    List<TaskResponse> listForUser();
    TaskResponse get(Long id);
    TaskMutationResponse create(TaskRequest request);
    TaskMutationResponse update(Long id, TaskRequest request);
    void delete(Long id);
    TaskMutationResponse changeStatus(Long id, TaskStatus status);
    TaskResponse reschedule(Long id, ReorderRequest request);
    Page<TaskResponse> search(LocalDate date, String title, Importance importance, TaskStatus status, Pageable pageable);
    List<AgendaOccurrence> getAgenda(LocalDate from, LocalDate to);
    List<TaskResponse> overdue();
    List<TaskResponse> today();
    SubtaskResponse addSubtask(Long taskId, SubtaskRequest request);
    SubtaskResponse updateSubtask(Long taskId, Long subtaskId, SubtaskRequest request);
    void deleteSubtask(Long taskId, Long subtaskId);
}
