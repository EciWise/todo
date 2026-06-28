package com.eciwise.todo.task;

import com.eciwise.todo.task.application.dto.TaskRequest;
import com.eciwise.todo.task.application.dto.TaskResponse;
import com.eciwise.todo.task.application.service.TaskService;
import com.eciwise.todo.task.domain.model.Importance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;

import static com.eciwise.todo.task.AuthTestSupport.authenticate;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TaskSearchTest {

    @Autowired
    private TaskService taskService;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void create(String title, Importance importance, LocalDate date) {
        taskService.create(new TaskRequest(title, null, null, importance, null,
                date, null, null, null, null,
                null, null, null, null, null, List.of(), List.of()));
    }

    @Test
    void buscaPorTituloImportanciaYFecha() {
        authenticate("search-1", "estudiante");
        LocalDate hoy = LocalDate.now();
        create("Estudiar Spring", Importance.HIGH, hoy);
        create("Estudiar Angular", Importance.LOW, hoy.plusDays(1));
        create("Comprar pan", Importance.HIGH, hoy);

        // Por titulo (case-insensitive, parcial).
        Page<TaskResponse> porTitulo = taskService.search(null, "estudiar", null, null, PageRequest.of(0, 10));
        assertThat(porTitulo.getContent()).extracting(TaskResponse::title)
                .containsExactlyInAnyOrder("Estudiar Spring", "Estudiar Angular");

        // Por importancia.
        Page<TaskResponse> alta = taskService.search(null, null, Importance.HIGH, null, PageRequest.of(0, 10));
        assertThat(alta.getContent()).extracting(TaskResponse::title)
                .containsExactlyInAnyOrder("Estudiar Spring", "Comprar pan");

        // Combinada: titulo + fecha + importancia.
        Page<TaskResponse> combo = taskService.search(hoy, "estudiar", Importance.HIGH, null, PageRequest.of(0, 10));
        assertThat(combo.getContent()).extracting(TaskResponse::title)
                .containsExactly("Estudiar Spring");
    }

    @Test
    void cadaUsuarioSoloVeSusTareas() {
        authenticate("search-a", "estudiante");
        create("De A", Importance.MEDIUM, LocalDate.now());

        authenticate("search-b", "estudiante");
        create("De B", Importance.MEDIUM, LocalDate.now());
        Page<TaskResponse> deB = taskService.search(null, null, null, null, PageRequest.of(0, 10));
        assertThat(deB.getContent()).extracting(TaskResponse::title).containsExactly("De B");
    }
}
