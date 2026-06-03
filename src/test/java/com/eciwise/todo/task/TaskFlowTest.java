package com.eciwise.todo.task;

import com.eciwise.todo.task.dto.ReorderRequest;
import com.eciwise.todo.task.dto.StatsResponse;
import com.eciwise.todo.task.dto.TaskMutationResponse;
import com.eciwise.todo.task.dto.TaskRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.eciwise.todo.task.AuthTestSupport.authenticate;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TaskFlowTest {

    @Autowired
    private TaskService taskService;
    @Autowired
    private StatsService statsService;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private TaskRequest simpleTask(String title) {
        return new TaskRequest(title, "desc", "notas", Importance.HIGH, null,
                null, null, null, "#ff0000", null,
                null, null, null, null, null, List.of("estudio"), List.of());
    }

    @Test
    void crearPlanificarYCompletarRegistraHistorial() {
        authenticate("flow-1", "estudiante");

        TaskRequest req = new TaskRequest("Estudiar JPA", "desc", null, Importance.MEDIUM, null,
                LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), "#00ff00", 0,
                null, null, null, null, null, List.of(), List.of());
        TaskMutationResponse created = taskService.create(req);
        assertThat(created.task().id()).isNotNull();
        assertThat(created.task().status()).isEqualTo(TaskStatus.PENDING);
        assertThat(created.task().scheduledDate()).isEqualTo(LocalDate.now());

        TaskMutationResponse done = taskService.changeStatus(created.task().id(), TaskStatus.DONE);
        assertThat(done.task().status()).isEqualTo(TaskStatus.DONE);
        assertThat(done.task().completedAt()).isNotNull();

        StatsResponse stats = statsService.forUser();
        assertThat(stats.done()).isEqualTo(1);
        assertThat(stats.currentStreakDays()).isEqualTo(1);
        assertThat(stats.completedByImportance().get(Importance.MEDIUM)).isEqualTo(1);
    }

    @Test
    void completarEsIdempotenteNoDuplicaHistorial() {
        authenticate("flow-2", "estudiante");
        TaskMutationResponse created = taskService.create(simpleTask("Una tarea"));

        taskService.changeStatus(created.task().id(), TaskStatus.DONE);
        taskService.changeStatus(created.task().id(), TaskStatus.IN_PROGRESS);
        taskService.changeStatus(created.task().id(), TaskStatus.DONE);

        StatsResponse stats = statsService.forUser();
        // El historial solo cuenta la primera vez que se completo.
        assertThat(stats.completedByImportance().values().stream().mapToLong(Long::longValue).sum())
                .isEqualTo(1);
    }

    @Test
    void reagendarMueveFechaYHora() {
        authenticate("flow-3", "estudiante");
        TaskMutationResponse created = taskService.create(simpleTask("Mover"));

        LocalDate target = LocalDate.now().plusDays(2);
        var moved = taskService.reschedule(created.task().id(),
                new ReorderRequest(target, LocalTime.of(15, 0), LocalTime.of(16, 0), 3));

        assertThat(moved.scheduledDate()).isEqualTo(target);
        assertThat(moved.startTime()).isEqualTo(LocalTime.of(15, 0));
        assertThat(moved.dayOrder()).isEqualTo(3);
    }

    @Test
    void agendaExpandeRecurrenciaSemanal() {
        authenticate("flow-4", "estudiante");
        LocalDate start = LocalDate.now();
        TaskRequest weekly = new TaskRequest("Repaso semanal", null, null, Importance.LOW, null,
                start, null, null, null, null,
                RecurrenceFreq.WEEKLY, 1, RecurrenceEndType.AFTER_COUNT, null, 4, List.of(), List.of());
        taskService.create(weekly);

        var occurrences = taskService.getAgenda(start, start.plusWeeks(5));
        assertThat(occurrences).hasSize(4);
        assertThat(occurrences.get(0).date()).isEqualTo(start);
        assertThat(occurrences.get(1).date()).isEqualTo(start.plusWeeks(1));
        assertThat(occurrences.get(1).virtual()).isTrue();
    }
}
