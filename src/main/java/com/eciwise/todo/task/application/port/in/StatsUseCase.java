package com.eciwise.todo.task.application.port.in;

import com.eciwise.todo.task.application.dto.StatsResponse;

public interface StatsUseCase {
    StatsResponse forUser();
}
