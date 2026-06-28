package com.eciwise.todo.task.infrastructure.out.persistence;

import com.eciwise.todo.task.application.port.out.SubtaskPort;
import com.eciwise.todo.task.domain.model.Subtask;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SubtaskRepositoryAdapter implements SubtaskPort {

    private final SubtaskJpaRepository repository;

    public SubtaskRepositoryAdapter(SubtaskJpaRepository repository) {
        this.repository = repository;
    }

    @Override public Subtask save(Subtask subtask) { return repository.save(subtask); }
    @Override public Optional<Subtask> findByIdAndTaskId(Long id, Long taskId) { return repository.findByIdAndTask_Id(id, taskId); }
    @Override public void delete(Subtask subtask) { repository.delete(subtask); }
}
