# ECIWISE-TODO

Task management microservice built with Spring Boot 3.3 and Java 21, following **hexagonal architecture** (ports and adapters).

---

## Architecture

```
com.eciwise.todo/
├── EciwiseTodoApplication.java
├── shared/
│   ├── auth/          JWT parsing, AuthenticatedUser record, Role enum
│   ├── config/        Security filter chain, JWT filter, JPA auditing
│   └── exception/     Global exception handler, custom exceptions
├── task/
│   ├── domain/model/  JPA entities + enums (Task, Subtask, Tag, TaskCategory,
│   │                  Achievement, TaskCompletionHistory, TaskStatus, Importance,
│   │                  RecurrenceFreq, RecurrenceEndType, AchievementType)
│   ├── application/
│   │   ├── dto/       Request / response records
│   │   ├── mapper/    TaskMapper (entity → DTO)
│   │   ├── port/in/   Use-case interfaces (TaskUseCase, TagUseCase, CategoryUseCase,
│   │   │              AchievementUseCase, StatsUseCase)
│   │   ├── port/out/  Output port interfaces (TaskPort, SubtaskPort, TagPort,
│   │   │              CategoryPort, AchievementPort, CompletionHistoryPort)
│   │   └── service/   TaskService, TagService, CategoryService, AchievementService,
│   │                  StatsService, RecurrenceExpander
│   └── infrastructure/
│       ├── in/rest/   REST controllers (TaskController, TagController,
│       │              CategoryController, AchievementController, StatsController)
│       └── out/persistence/  JPA repositories + adapter implementations
└── user/
    ├── domain/model/        AppUser entity
    ├── application/
    │   ├── port/out/        UserPort interface
    │   └── service/         CurrentUserService
    └── infrastructure/persistence/  AppUserJpaRepository + UserRepositoryAdapter
```

### Hexagonal principles applied

- **Domain layer** (`domain/model`) has zero framework dependencies beyond JPA annotations.
- **Application layer** (`application/`) depends only on the domain and port interfaces — never on Spring Data or infrastructure classes.
- **Infrastructure layer** (`infrastructure/`) wires Spring Data JPA repositories into adapters that implement the output ports.
- **Input adapters** (`infrastructure/in/rest/`) call use-case interfaces, never service classes directly (controllers depend on port/in interfaces).

---

## Tech stack

| Technology | Version |
|---|---|
| Java | 21 |
| Spring Boot | 3.3 |
| Spring Data JPA | via Boot |
| Spring Security | via Boot |
| jjwt (JWT) | 0.12.x |
| Lombok | latest |
| H2 (tests) | via Boot test |
| Docker | multi-stage |

---

## Setup

### Prerequisites

- Java 21
- Maven 3.9+ (or use the included `./mvnw` wrapper)
- A running PostgreSQL instance (or configure another datasource)

### Configuration

Copy `src/main/resources/application.properties` and set:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/eciwise_todo
spring.datasource.username=<user>
spring.datasource.password=<password>
spring.jpa.hibernate.ddl-auto=validate

jwt.secret=<base64-encoded-secret-or-plain-string>
app.cors.allowed-origins=http://localhost:4200
```

### Run locally

```bash
./mvnw spring-boot:run
```

### Run tests

```bash
./mvnw test
```

### Build Docker image

```bash
docker build -t eciwise-todo .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/eciwise_todo \
  -e SPRING_DATASOURCE_USERNAME=... \
  -e SPRING_DATASOURCE_PASSWORD=... \
  -e JWT_SECRET=... \
  eciwise-todo
```

---

## API Endpoints

All endpoints require a valid JWT in the `Authorization: Bearer <token>` header.  
Accepted roles: `estudiante`, `tutor`, `admin`.

### Tasks — `/api/tasks`

| Method | Path | Description |
|---|---|---|
| GET | `/api/tasks` | List all tasks for the authenticated user |
| GET | `/api/tasks/{id}` | Get a single task |
| POST | `/api/tasks` | Create a task |
| PUT | `/api/tasks/{id}` | Update a task |
| DELETE | `/api/tasks/{id}` | Delete a task |
| PATCH | `/api/tasks/{id}/status` | Change task status (`PENDING`, `IN_PROGRESS`, `DONE`) |
| PUT | `/api/tasks/{id}/schedule` | Reschedule / reorder a task |
| GET | `/api/tasks/search` | Search tasks (`date`, `title`, `importance`, `status`, `page`, `size`) |
| GET | `/api/tasks/agenda` | Expand recurring tasks over a date range (`from`, `to`) |
| GET | `/api/tasks/overdue` | Tasks past their scheduled date and not done |
| GET | `/api/tasks/today` | Tasks scheduled for today |
| POST | `/api/tasks/{id}/subtasks` | Add a subtask |
| PUT | `/api/tasks/{id}/subtasks/{subtaskId}` | Update a subtask |
| DELETE | `/api/tasks/{id}/subtasks/{subtaskId}` | Delete a subtask |

### Tags — `/api/tags`

| Method | Path | Description |
|---|---|---|
| GET | `/api/tags` | List tags |
| POST | `/api/tags` | Create a tag |
| DELETE | `/api/tags/{id}` | Delete a tag |

### Categories — `/api/categories`

| Method | Path | Description |
|---|---|---|
| GET | `/api/categories` | List categories |
| POST | `/api/categories` | Create a category |
| PUT | `/api/categories/{id}` | Update a category |
| DELETE | `/api/categories/{id}` | Delete a category |

### Achievements — `/api/achievements`

| Method | Path | Description |
|---|---|---|
| GET | `/api/achievements` | List achievements for the user |

Achievements are awarded automatically when a user completes or plans a multiple of 5 tasks.

### Stats — `/api/stats`

| Method | Path | Description |
|---|---|---|
| GET | `/api/stats` | Summary statistics (totals, completion rate, streak, overdue, etc.) |

---

## CI / CD

The `.github/workflows/publish.yml` workflow:

1. Triggers on push to `main` or a published GitHub Release.
2. Logs in to GitHub Container Registry (GHCR).
3. Builds the Docker image using `docker/build-push-action`.
4. Pushes with tags: branch name, semantic version, and short SHA.

The image is published to `ghcr.io/<owner>/<repo>`.

---

## Package structure (legacy → hexagonal mapping)

| Old path | New path |
|---|---|
| `com.eciwise.todo.auth.*` | `com.eciwise.todo.shared.auth.*` |
| `com.eciwise.todo.config.*` | `com.eciwise.todo.shared.config.*` |
| `com.eciwise.todo.exception.*` | `com.eciwise.todo.shared.exception.*` |
| `com.eciwise.todo.task.Task` (entity) | `com.eciwise.todo.task.domain.model.Task` |
| `com.eciwise.todo.task.dto.*` | `com.eciwise.todo.task.application.dto.*` |
| `com.eciwise.todo.task.TaskMapper` | `com.eciwise.todo.task.application.mapper.TaskMapper` |
| `com.eciwise.todo.task.TaskService` | `com.eciwise.todo.task.application.service.TaskService` |
| `com.eciwise.todo.task.TaskController` | `com.eciwise.todo.task.infrastructure.in.rest.TaskController` |
| `com.eciwise.todo.task.TaskRepository` | `com.eciwise.todo.task.infrastructure.out.persistence.TaskJpaRepository` + `TaskRepositoryAdapter` |
| `com.eciwise.todo.user.AppUser` | `com.eciwise.todo.user.domain.model.AppUser` |
| `com.eciwise.todo.user.AppUserRepository` | `com.eciwise.todo.user.infrastructure.persistence.AppUserJpaRepository` + `UserRepositoryAdapter` |
| `com.eciwise.todo.user.CurrentUserService` | `com.eciwise.todo.user.application.service.CurrentUserService` |
