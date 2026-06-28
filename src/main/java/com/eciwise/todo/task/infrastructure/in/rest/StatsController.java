package com.eciwise.todo.task.infrastructure.in.rest;

import com.eciwise.todo.task.application.dto.StatsResponse;
import com.eciwise.todo.task.application.port.in.StatsUseCase;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsUseCase statsUseCase;

    public StatsController(StatsUseCase statsUseCase) { this.statsUseCase = statsUseCase; }

    @GetMapping
    public StatsResponse stats() { return statsUseCase.forUser(); }
}
