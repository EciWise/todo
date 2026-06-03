package com.eciwise.todo.task;

import com.eciwise.todo.exception.ResourceNotFoundException;
import com.eciwise.todo.task.dto.AchievementResponse;
import com.eciwise.todo.task.dto.AgendaOccurrence;
import com.eciwise.todo.task.dto.ReorderRequest;
import com.eciwise.todo.task.dto.SubtaskRequest;
import com.eciwise.todo.task.dto.SubtaskResponse;
import com.eciwise.todo.task.dto.TaskMutationResponse;
import com.eciwise.todo.task.dto.TaskRequest;
import com.eciwise.todo.task.dto.TaskResponse;
import com.eciwise.todo.user.AppUser;
import com.eciwise.todo.user.CurrentUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
public class TaskService {

    private final TaskRepository taskRepository;
    private final SubtaskRepository subtaskRepository;
    private final TaskCompletionHistoryRepository historyRepository;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final AchievementService achievementService;
    private final RecurrenceExpander recurrenceExpander;
    private final CurrentUserService currentUserService;
    private final TaskMapper mapper;

    public TaskService(TaskRepository taskRepository,
                       SubtaskRepository subtaskRepository,
                       TaskCompletionHistoryRepository historyRepository,
                       CategoryService categoryService,
                       TagService tagService,
                       AchievementService achievementService,
                       RecurrenceExpander recurrenceExpander,
                       CurrentUserService currentUserService,
                       TaskMapper mapper) {
        this.taskRepository = taskRepository;
        this.subtaskRepository = subtaskRepository;
        this.historyRepository = historyRepository;
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.achievementService = achievementService;
        this.recurrenceExpander = recurrenceExpander;
        this.currentUserService = currentUserService;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listForUser() {
        AppUser user = currentUserService.getOrCreate();
        return taskRepository.findByOwner_IdOrderByScheduledDateAscStartTimeAscDayOrderAsc(user.getId())
                .stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse get(Long id) {
        AppUser user = currentUserService.getOrCreate();
        return mapper.toResponse(requireOwned(id, user));
    }

    @Transactional
    public TaskMutationResponse create(TaskRequest request) {
        AppUser user = currentUserService.getOrCreate();
        Task task = Task.builder()
                .owner(user)
                .title(request.title())
                .build();
        applyRequest(task, request, user);

        List<Achievement> awarded = new ArrayList<>();
        // Si nace planificada, cuenta para el logro de planificacion.
        if (task.getScheduledDate() != null && !task.isPlannedNotified()) {
            task.setPlannedNotified(true);
        }
        task = taskRepository.save(task);
        if (task.isPlannedNotified()) {
            achievementService.onTaskPlanned(user).ifPresent(awarded::add);
        }
        return mutation(task, awarded);
    }

    @Transactional
    public TaskMutationResponse update(Long id, TaskRequest request) {
        AppUser user = currentUserService.getOrCreate();
        Task task = requireOwned(id, user);
        boolean wasPlanned = task.isPlannedNotified();
        applyRequest(task, request, user);

        List<Achievement> awarded = new ArrayList<>();
        if (task.getScheduledDate() != null && !wasPlanned) {
            task.setPlannedNotified(true);
        }
        task = taskRepository.save(task);
        if (task.isPlannedNotified() && !wasPlanned) {
            achievementService.onTaskPlanned(user).ifPresent(awarded::add);
        }
        return mutation(task, awarded);
    }

    @Transactional
    public void delete(Long id) {
        AppUser user = currentUserService.getOrCreate();
        taskRepository.delete(requireOwned(id, user));
    }

    @Transactional
    public TaskMutationResponse changeStatus(Long id, TaskStatus status) {
        AppUser user = currentUserService.getOrCreate();
        Task task = requireOwned(id, user);
        List<Achievement> awarded = new ArrayList<>();

        if (status == TaskStatus.DONE && !task.isCompletedNotified()) {
            task.setStatus(TaskStatus.DONE);
            task.setCompletedAt(Instant.now());
            task.setCompletedNotified(true);
            taskRepository.save(task);
            recordCompletion(user, task);
            achievementService.onTaskCompleted(user).ifPresent(awarded::add);
        } else {
            task.setStatus(status);
            if (status != TaskStatus.DONE) {
                task.setCompletedAt(null);
            }
            taskRepository.save(task);
        }
        return mutation(task, awarded);
    }

    @Transactional
    public TaskResponse reschedule(Long id, ReorderRequest request) {
        AppUser user = currentUserService.getOrCreate();
        Task task = requireOwned(id, user);
        task.setScheduledDate(request.scheduledDate());
        task.setStartTime(request.startTime());
        task.setEndTime(request.endTime());
        if (request.dayOrder() != null) {
            task.setDayOrder(request.dayOrder());
        }
        if (task.getScheduledDate() != null && !task.isPlannedNotified()) {
            task.setPlannedNotified(true);
        }
        return mapper.toResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> search(LocalDate date, String title, Importance importance,
                                     TaskStatus status, Pageable pageable) {
        AppUser user = currentUserService.getOrCreate();
        Specification<Task> spec = Specification.where(TaskSpecifications.ownedBy(user.getId()))
                .and(TaskSpecifications.onDate(date))
                .and(TaskSpecifications.titleContains(title))
                .and(TaskSpecifications.hasImportance(importance))
                .and(TaskSpecifications.hasStatus(status));
        return taskRepository.findAll(spec, pageable).map(mapper::toResponse);
    }

    /** Expande las tareas (incluidas recurrentes) en ocurrencias dentro del rango. */
    @Transactional(readOnly = true)
    public List<AgendaOccurrence> getAgenda(LocalDate from, LocalDate to) {
        AppUser user = currentUserService.getOrCreate();
        if (from == null || to == null || to.isBefore(from)) {
            throw new IllegalArgumentException("Rango de fechas invalido");
        }
        List<AgendaOccurrence> result = new ArrayList<>();
        for (Task task : taskRepository.findByOwner_IdOrderByScheduledDateAscStartTimeAscDayOrderAsc(user.getId())) {
            if (task.getScheduledDate() == null) {
                continue;
            }
            TaskResponse response = mapper.toResponse(task);
            for (LocalDate date : recurrenceExpander.expand(task, from, to)) {
                boolean virtual = !date.equals(task.getScheduledDate());
                result.add(new AgendaOccurrence(date, virtual, response));
            }
        }
        result.sort(Comparator.comparing(AgendaOccurrence::date)
                .thenComparing(o -> o.task().startTime(), Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingInt(o -> o.task().dayOrder()));
        return result;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> overdue() {
        AppUser user = currentUserService.getOrCreate();
        return taskRepository.findByOwner_IdAndScheduledDateBeforeAndStatusNot(
                        user.getId(), LocalDate.now(), TaskStatus.DONE)
                .stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> today() {
        AppUser user = currentUserService.getOrCreate();
        return taskRepository.findByOwner_IdAndScheduledDate(user.getId(), LocalDate.now())
                .stream().map(mapper::toResponse).toList();
    }

    // --- Subtareas ---

    @Transactional
    public SubtaskResponse addSubtask(Long taskId, SubtaskRequest request) {
        AppUser user = currentUserService.getOrCreate();
        Task task = requireOwned(taskId, user);
        Subtask subtask = Subtask.builder()
                .task(task)
                .title(request.title())
                .done(Boolean.TRUE.equals(request.done()))
                .position(request.position() != null ? request.position() : task.getSubtasks().size())
                .build();
        return mapper.toSubtaskResponse(subtaskRepository.save(subtask));
    }

    @Transactional
    public SubtaskResponse updateSubtask(Long taskId, Long subtaskId, SubtaskRequest request) {
        AppUser user = currentUserService.getOrCreate();
        requireOwned(taskId, user);
        Subtask subtask = subtaskRepository.findByIdAndTask_Id(subtaskId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtarea no encontrada: " + subtaskId));
        subtask.setTitle(request.title());
        if (request.done() != null) {
            subtask.setDone(request.done());
        }
        if (request.position() != null) {
            subtask.setPosition(request.position());
        }
        return mapper.toSubtaskResponse(subtaskRepository.save(subtask));
    }

    @Transactional
    public void deleteSubtask(Long taskId, Long subtaskId) {
        AppUser user = currentUserService.getOrCreate();
        requireOwned(taskId, user);
        Subtask subtask = subtaskRepository.findByIdAndTask_Id(subtaskId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtarea no encontrada: " + subtaskId));
        subtaskRepository.delete(subtask);
    }

    // --- Helpers ---

    private void applyRequest(Task task, TaskRequest request, AppUser user) {
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setNotes(request.notes());
        if (request.importance() != null) {
            task.setImportance(request.importance());
        }
        task.setScheduledDate(request.scheduledDate());
        task.setStartTime(request.startTime());
        task.setEndTime(request.endTime());
        task.setColor(request.color());
        if (request.dayOrder() != null) {
            task.setDayOrder(request.dayOrder());
        }
        applyRecurrence(task, request);
        applyCategory(task, request, user);
        applyTags(task, request, user);
        applySubtasks(task, request);
    }

    private void applyRecurrence(Task task, TaskRequest request) {
        task.setRecurrenceFreq(request.recurrenceFreq() != null ? request.recurrenceFreq() : RecurrenceFreq.NONE);
        task.setRecurrenceInterval(request.recurrenceInterval() != null ? Math.max(1, request.recurrenceInterval()) : 1);
        task.setRecurrenceEndType(request.recurrenceEndType() != null ? request.recurrenceEndType() : RecurrenceEndType.NEVER);
        task.setRecurrenceEndDate(request.recurrenceEndDate());
        task.setRecurrenceCount(request.recurrenceCount());
    }

    private void applyCategory(Task task, TaskRequest request, AppUser user) {
        if (request.categoryId() == null) {
            task.setCategory(null);
        } else {
            task.setCategory(categoryService.requireOwned(request.categoryId(), user));
        }
    }

    private void applyTags(Task task, TaskRequest request, AppUser user) {
        if (request.tags() == null) {
            return;
        }
        Set<Tag> tags = new LinkedHashSet<>();
        for (String name : request.tags()) {
            if (name != null && !name.isBlank()) {
                tags.add(tagService.getOrCreate(user, name));
            }
        }
        task.getTags().clear();
        task.getTags().addAll(tags);
    }

    private void applySubtasks(Task task, TaskRequest request) {
        if (request.subtasks() == null) {
            return;
        }
        task.getSubtasks().clear();
        int position = 0;
        for (SubtaskRequest sr : request.subtasks()) {
            if (sr == null || sr.title() == null || sr.title().isBlank()) {
                continue;
            }
            task.getSubtasks().add(Subtask.builder()
                    .task(task)
                    .title(sr.title())
                    .done(Boolean.TRUE.equals(sr.done()))
                    .position(sr.position() != null ? sr.position() : position++)
                    .build());
        }
    }

    private void recordCompletion(AppUser user, Task task) {
        historyRepository.save(TaskCompletionHistory.builder()
                .owner(user)
                .task(task)
                .title(task.getTitle())
                .importance(task.getImportance())
                .completedAt(task.getCompletedAt())
                .build());
    }

    private Task requireOwned(Long id, AppUser user) {
        return taskRepository.findByIdAndOwner_Id(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada: " + id));
    }

    private TaskMutationResponse mutation(Task task, List<Achievement> awarded) {
        List<AchievementResponse> achievements = awarded.stream()
                .map(mapper::toAchievementResponse)
                .toList();
        return new TaskMutationResponse(mapper.toResponse(task), achievements);
    }
}
