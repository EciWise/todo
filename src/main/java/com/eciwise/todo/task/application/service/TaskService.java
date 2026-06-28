package com.eciwise.todo.task.application.service;

import com.eciwise.todo.shared.exception.ResourceNotFoundException;
import com.eciwise.todo.task.application.dto.*;
import com.eciwise.todo.task.application.mapper.TaskMapper;
import com.eciwise.todo.task.application.port.in.AchievementUseCase;
import com.eciwise.todo.task.application.port.in.TaskUseCase;
import com.eciwise.todo.task.application.port.out.*;
import com.eciwise.todo.task.domain.model.*;
import com.eciwise.todo.user.application.service.CurrentUserService;
import com.eciwise.todo.user.domain.model.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class TaskService implements TaskUseCase {

    private final TaskPort taskPort;
    private final SubtaskPort subtaskPort;
    private final CompletionHistoryPort historyPort;
    private final CategoryPort categoryPort;
    private final TagPort tagPort;
    private final AchievementUseCase achievementUseCase;
    private final RecurrenceExpander recurrenceExpander;
    private final CurrentUserService currentUserService;
    private final TaskMapper mapper;

    public TaskService(TaskPort taskPort, SubtaskPort subtaskPort, CompletionHistoryPort historyPort,
                       CategoryPort categoryPort, TagPort tagPort, AchievementUseCase achievementUseCase,
                       RecurrenceExpander recurrenceExpander, CurrentUserService currentUserService,
                       TaskMapper mapper) {
        this.taskPort = taskPort;
        this.subtaskPort = subtaskPort;
        this.historyPort = historyPort;
        this.categoryPort = categoryPort;
        this.tagPort = tagPort;
        this.achievementUseCase = achievementUseCase;
        this.recurrenceExpander = recurrenceExpander;
        this.currentUserService = currentUserService;
        this.mapper = mapper;
    }

    @Override @Transactional(readOnly = true)
    public List<TaskResponse> listForUser() {
        AppUser user = currentUserService.getOrCreate();
        return taskPort.findByOwnerIdOrdered(user.getId()).stream().map(mapper::toResponse).toList();
    }

    @Override @Transactional(readOnly = true)
    public TaskResponse get(Long id) {
        return mapper.toResponse(requireOwned(id, currentUserService.getOrCreate()));
    }

    @Override @Transactional
    public TaskMutationResponse create(TaskRequest request) {
        AppUser user = currentUserService.getOrCreate();
        Task task = Task.builder().owner(user).title(request.title()).build();
        applyRequest(task, request, user);
        List<Achievement> awarded = new ArrayList<>();
        if (task.getScheduledDate() != null && !task.isPlannedNotified()) task.setPlannedNotified(true);
        task = taskPort.save(task);
        if (task.isPlannedNotified()) achievementUseCase.onTaskPlanned(user).ifPresent(awarded::add);
        return mutation(task, awarded);
    }

    @Override @Transactional
    public TaskMutationResponse update(Long id, TaskRequest request) {
        AppUser user = currentUserService.getOrCreate();
        Task task = requireOwned(id, user);
        boolean wasPlanned = task.isPlannedNotified();
        applyRequest(task, request, user);
        List<Achievement> awarded = new ArrayList<>();
        if (task.getScheduledDate() != null && !wasPlanned) task.setPlannedNotified(true);
        task = taskPort.save(task);
        if (task.isPlannedNotified() && !wasPlanned) achievementUseCase.onTaskPlanned(user).ifPresent(awarded::add);
        return mutation(task, awarded);
    }

    @Override @Transactional
    public void delete(Long id) {
        AppUser user = currentUserService.getOrCreate();
        taskPort.delete(requireOwned(id, user));
    }

    @Override @Transactional
    public TaskMutationResponse changeStatus(Long id, TaskStatus status) {
        AppUser user = currentUserService.getOrCreate();
        Task task = requireOwned(id, user);
        List<Achievement> awarded = new ArrayList<>();
        if (status == TaskStatus.DONE && !task.isCompletedNotified()) {
            task.setStatus(TaskStatus.DONE);
            task.setCompletedAt(Instant.now());
            task.setCompletedNotified(true);
            taskPort.save(task);
            historyPort.save(TaskCompletionHistory.builder()
                    .owner(user).task(task).title(task.getTitle())
                    .importance(task.getImportance()).completedAt(task.getCompletedAt()).build());
            achievementUseCase.onTaskCompleted(user).ifPresent(awarded::add);
        } else {
            task.setStatus(status);
            if (status != TaskStatus.DONE) task.setCompletedAt(null);
            taskPort.save(task);
        }
        return mutation(task, awarded);
    }

    @Override @Transactional
    public TaskResponse reschedule(Long id, ReorderRequest request) {
        AppUser user = currentUserService.getOrCreate();
        Task task = requireOwned(id, user);
        task.setScheduledDate(request.scheduledDate());
        task.setStartTime(request.startTime());
        task.setEndTime(request.endTime());
        if (request.dayOrder() != null) task.setDayOrder(request.dayOrder());
        if (task.getScheduledDate() != null && !task.isPlannedNotified()) task.setPlannedNotified(true);
        return mapper.toResponse(taskPort.save(task));
    }

    @Override @Transactional(readOnly = true)
    public Page<TaskResponse> search(LocalDate date, String title, Importance importance,
                                     TaskStatus status, Pageable pageable) {
        AppUser user = currentUserService.getOrCreate();
        return taskPort.search(user.getId(), date, title, importance, status, pageable).map(mapper::toResponse);
    }

    @Override @Transactional(readOnly = true)
    public List<AgendaOccurrence> getAgenda(LocalDate from, LocalDate to) {
        AppUser user = currentUserService.getOrCreate();
        if (from == null || to == null || to.isBefore(from))
            throw new IllegalArgumentException("Rango de fechas invalido");
        List<AgendaOccurrence> result = new ArrayList<>();
        for (Task task : taskPort.findByOwnerIdOrdered(user.getId())) {
            if (task.getScheduledDate() == null) continue;
            TaskResponse response = mapper.toResponse(task);
            for (LocalDate date : recurrenceExpander.expand(task, from, to)) {
                result.add(new AgendaOccurrence(date, !date.equals(task.getScheduledDate()), response));
            }
        }
        result.sort(Comparator.comparing(AgendaOccurrence::date)
                .thenComparing(o -> o.task().startTime(), Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingInt(o -> o.task().dayOrder()));
        return result;
    }

    @Override @Transactional(readOnly = true)
    public List<TaskResponse> overdue() {
        AppUser user = currentUserService.getOrCreate();
        return taskPort.findOverdue(user.getId(), LocalDate.now(), TaskStatus.DONE).stream().map(mapper::toResponse).toList();
    }

    @Override @Transactional(readOnly = true)
    public List<TaskResponse> today() {
        AppUser user = currentUserService.getOrCreate();
        return taskPort.findByOwnerIdAndDate(user.getId(), LocalDate.now()).stream().map(mapper::toResponse).toList();
    }

    @Override @Transactional
    public SubtaskResponse addSubtask(Long taskId, SubtaskRequest request) {
        AppUser user = currentUserService.getOrCreate();
        Task task = requireOwned(taskId, user);
        Subtask subtask = Subtask.builder().task(task).title(request.title())
                .done(Boolean.TRUE.equals(request.done()))
                .position(request.position() != null ? request.position() : task.getSubtasks().size()).build();
        return mapper.toSubtaskResponse(subtaskPort.save(subtask));
    }

    @Override @Transactional
    public SubtaskResponse updateSubtask(Long taskId, Long subtaskId, SubtaskRequest request) {
        AppUser user = currentUserService.getOrCreate();
        requireOwned(taskId, user);
        Subtask subtask = subtaskPort.findByIdAndTaskId(subtaskId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtarea no encontrada: " + subtaskId));
        subtask.setTitle(request.title());
        if (request.done() != null) subtask.setDone(request.done());
        if (request.position() != null) subtask.setPosition(request.position());
        return mapper.toSubtaskResponse(subtaskPort.save(subtask));
    }

    @Override @Transactional
    public void deleteSubtask(Long taskId, Long subtaskId) {
        AppUser user = currentUserService.getOrCreate();
        requireOwned(taskId, user);
        Subtask subtask = subtaskPort.findByIdAndTaskId(subtaskId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtarea no encontrada: " + subtaskId));
        subtaskPort.delete(subtask);
    }

    private void applyRequest(Task task, TaskRequest request, AppUser user) {
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setNotes(request.notes());
        if (request.importance() != null) task.setImportance(request.importance());
        task.setScheduledDate(request.scheduledDate());
        task.setStartTime(request.startTime());
        task.setEndTime(request.endTime());
        task.setColor(request.color());
        if (request.dayOrder() != null) task.setDayOrder(request.dayOrder());
        task.setRecurrenceFreq(request.recurrenceFreq() != null ? request.recurrenceFreq() : RecurrenceFreq.NONE);
        task.setRecurrenceInterval(request.recurrenceInterval() != null ? Math.max(1, request.recurrenceInterval()) : 1);
        task.setRecurrenceEndType(request.recurrenceEndType() != null ? request.recurrenceEndType() : RecurrenceEndType.NEVER);
        task.setRecurrenceEndDate(request.recurrenceEndDate());
        task.setRecurrenceCount(request.recurrenceCount());
        if (request.categoryId() == null) {
            task.setCategory(null);
        } else {
            task.setCategory(categoryPort.findByIdAndOwnerId(request.categoryId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada: " + request.categoryId())));
        }
        if (request.tags() != null) {
            Set<Tag> tags = new LinkedHashSet<>();
            for (String name : request.tags()) {
                if (name != null && !name.isBlank()) {
                    String trimmed = name.trim();
                    tags.add(tagPort.findByOwnerIdAndName(user.getId(), trimmed)
                            .orElseGet(() -> tagPort.save(Tag.builder().owner(user).name(trimmed).build())));
                }
            }
            task.getTags().clear();
            task.getTags().addAll(tags);
        }
        if (request.subtasks() != null) {
            task.getSubtasks().clear();
            int pos = 0;
            for (SubtaskRequest sr : request.subtasks()) {
                if (sr == null || sr.title() == null || sr.title().isBlank()) continue;
                task.getSubtasks().add(Subtask.builder().task(task).title(sr.title())
                        .done(Boolean.TRUE.equals(sr.done()))
                        .position(sr.position() != null ? sr.position() : pos++).build());
            }
        }
    }

    private Task requireOwned(Long id, AppUser user) {
        return taskPort.findByIdAndOwnerId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada: " + id));
    }

    private TaskMutationResponse mutation(Task task, List<Achievement> awarded) {
        return new TaskMutationResponse(mapper.toResponse(task),
                awarded.stream().map(mapper::toAchievementResponse).toList());
    }
}
