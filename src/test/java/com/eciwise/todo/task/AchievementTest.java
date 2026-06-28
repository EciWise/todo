package com.eciwise.todo.task;

import com.eciwise.todo.task.application.dto.AchievementResponse;
import com.eciwise.todo.task.application.dto.TaskMutationResponse;
import com.eciwise.todo.task.application.dto.TaskRequest;
import com.eciwise.todo.task.application.service.AchievementService;
import com.eciwise.todo.task.application.service.TaskService;
import com.eciwise.todo.task.domain.model.AchievementType;
import com.eciwise.todo.task.domain.model.Importance;
import com.eciwise.todo.task.domain.model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;

import static com.eciwise.todo.task.AuthTestSupport.authenticate;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AchievementTest {

    @Autowired
    private TaskService taskService;
    @Autowired
    private AchievementService achievementService;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private TaskRequest unplanned(String title) {
        return new TaskRequest(title, null, null, Importance.MEDIUM, null,
                null, null, null, null, null,
                null, null, null, null, null, List.of(), List.of());
    }

    private TaskRequest planned(String title) {
        return new TaskRequest(title, null, null, Importance.MEDIUM, null,
                LocalDate.now(), null, null, null, null,
                null, null, null, null, null, List.of(), List.of());
    }

    @Test
    void completarCincoTareasOtorgaFelicitacion() {
        authenticate("ach-complete", "estudiante");

        AchievementResponse fifth = null;
        for (int i = 1; i <= 5; i++) {
            TaskMutationResponse created = taskService.create(unplanned("T" + i));
            TaskMutationResponse done = taskService.changeStatus(created.task().id(), TaskStatus.DONE);
            if (i < 5) {
                assertThat(done.achievements()).isEmpty();
            } else {
                assertThat(done.achievements()).hasSize(1);
                fifth = done.achievements().get(0);
            }
        }

        assertThat(fifth).isNotNull();
        assertThat(fifth.type()).isEqualTo(AchievementType.TASKS_COMPLETED);
        assertThat(fifth.milestone()).isEqualTo(5);
    }

    @Test
    void planificarCincoTareasOtorgaFelicitacionIndependiente() {
        authenticate("ach-plan", "estudiante");

        TaskMutationResponse last = null;
        for (int i = 1; i <= 5; i++) {
            last = taskService.create(planned("P" + i));
        }

        assertThat(last).isNotNull();
        assertThat(last.achievements()).hasSize(1);
        assertThat(last.achievements().get(0).type()).isEqualTo(AchievementType.TASKS_PLANNED);
        assertThat(last.achievements().get(0).milestone()).isEqualTo(5);

        // Solo hay un logro de planificacion (no se mezcla con completar).
        List<AchievementResponse> all = achievementService.listForUser();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).type()).isEqualTo(AchievementType.TASKS_PLANNED);
    }
}
