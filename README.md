# Cascada

Cascada is a core Java backend task and reminder management application built around object-oriented principles, explicit ORM persistence, concurrency patterns, custom data structures, and functional domain aggregations.

The project demonstrates low-level backend design without Spring Framework dependencies, using pure Java 26, JPA (Jakarta Persistence), Hibernate, and Maven.

---

## Architectural Philosophy & Core Decisions

### Rich Domain Model vs. Anemic Data Holders
Cascada enforces a **Rich Domain Model** as required. Entity logic and state-transition rules reside directly inside entity objects (e.g., `TaskEntity`) rather than being shifted into external services. Service classes coordinate persistence, dispatch asynchronous events, handle transaction boundaries, and execute multi-entity queries, leaving core business assertions inside the domain layer.

### Package-by-Feature Design
The project structure uses a **Package-by-Feature** strategy rather than traditional layer-based separation. Feature slices contain their associated entities, repositories, state handlers, and use-case orchestrators in isolated packages (`features.task`, `features.user`, `features.reminder`). This minimizes cross-module coupling and enforces explicit boundary rules.

```
com.cascada.core
├── common
│   ├── jparepo         # Generic Repository abstraction over JPA EntityManager
│   ├── event           # Thread-safe Singleton EventBus, Observers, and Event Config
│   └── sorting         # Explicit Merge Sort implementation for Task entities
├── domain
│   ├── enums           # Priority, TaskStatus, NotificationChannel
│   └── exception       # Exception hierarchy anchored by GlobalException
├── features
│   ├── user            # UserEntity, UserRepository, UserService
│   ├── task
│   │   ├── entity      # TaskEntity rich domain model
│   │   ├── states      # GoF State Pattern implementations
│   │   ├── data        # TaskRepository
│   │   ├── event       # Sealed TaskEvent hierarchy and records
│   │   └── usecase     # TaskService and TaskAnalyticsService
│   └── reminder
│       ├── entity      # ReminderEntity
│       ├── data        # ReminderRepository
│       └── usecase     # Multi-threaded ReminderDispatcher
├── notification
│   ├── factory         # ReminderFactory and channel-specific NotificationSenders
│   └── observer        # Concrete task event observers (Logger, Notifier)
├── validation          # Stateless Email validation utilities
├── cli                 # Interactive CLI interface
└── CascadaApplication.java # Manual dependency wiring and runtime bootstrapper
```

### Persistence Choice: Why JPA / Hibernate over Raw JDBC?
The project explicitly chose **JPA (Jakarta Persistence) with Hibernate** over raw JDBC:
* **Boilerplate Reduction:** Eliminates repetitive SQL string concats and manual `ResultSet` mapping while maintaining full explicit control over database operations.
* **Pure ORM Learning:** Using direct `EntityManager` methods (`persist`, `merge`, `find`, `createQuery`) without Spring Data auto-generation provides hands-on mastery over ORM entity lifecycles, persistence contexts, and JPQL queries.
* **Seamless H2 Integration:** Hibernate handles automatic schema generation (`hbm2ddl.auto=update`) directly against the in-memory H2 database.

### SQL Keyword Prevention & Naming Conventions
All persistent domain models are suffixed with `Entity` (`UserEntity`, `TaskEntity`, `ReminderEntity`). This explicit convention prevents SQL reserved-keyword collisions in standard SQL engines (such as the reserved `USER` keyword in H2/PostgreSQL dialects).

---

## Design Patterns

### 1. Required Pattern: Factory Pattern
The `ReminderFactory` evaluates the `NotificationChannel` (`EMAIL`, `SMS`, `PUSH`) associated with a `ReminderEntity` and returns the matching `ReminderSender` strategy implementation (`EmailReminderSender`, `SmsReminderSender`, `PushReminderSender`).

### 2. Required Pattern: Observer / Pub-Sub Event Bus
The event bus decouples task lifecycle state events (`Created`, `Broadcasted`, `Claimed`, `Completed`, `Cancelled`) from reactive side-effects:

* `EventBus`: Implemented as a thread-safe Singleton using `CopyOnWriteArrayList<TaskEventObserver>` to handle observer registration and concurrent dispatch safely.
* Sealed Interface Events: `TaskEvent` is defined as a sealed interface enclosing immutable record types (`Created`, `Broadcasted`, `Claimed`, `Completed`, `Cancelled`). This provides compile-time exhaustive checking when pattern-matching over event types.
* Observers: `TaskEventLogger` logs execution states, `BroadcastNotifier` filters high-priority task events (`Priority.HIGH`) to notify connected users, and `ReminderCreationObserver` attaches default reminder obligations upon claiming.

### 3. Bonus Pattern: State Pattern (Task Lifecycle)
The lifecycle of a task spans five states: `TODO`, `IN_PROGRESS`, `DONE`, `CANCELLED`, and `OVERDUE`. To eliminate conditional branches and scatter-shot flag checks, the GoF State Pattern was chosen as the bonus pattern:

* `TaskState` Interface: Defines state transition contracts (`markInProgress`, `markDone`, `cancel`, `markOverdue`, `postpone`).
* Concrete Implementations: `TodoState`, `InProgressState`, `DoneState`, `CancelledState`, and `OverdueState`.
* JPA Synchronization: A `@Transient TaskState state` field handles behavior, while a persistent `TaskStatus status` enum stores the string representation. The `@PostLoad` JPA lifecycle callback invokes a factory `TaskState.of(status)` to reconstruct behavior instances automatically when loaded from the database.

---

## The Judgment Call (Open-Ended Product Decision)

### Append-Only Reminder Audit History
Every task manager must decide what happens to pending reminders when a task's due date is pushed back. In Cascada, instead of updating or overwriting existing reminder records, an **append-only model** (`List<ReminderEntity>`) was implemented. Every call to `TaskEntity.postpone(LocalDateTime newDueDate)` appends a newly generated `ReminderEntity` scheduled prior to the new due date.

* **Reasoning:** Zero user friction (no manual reminder editing needed), an automatic audit trail tracking how many times a task was delayed and when, and complete historical data retention as a natural side effect of domain logic.
* **Trade-off:** Retains historical reminder instances in memory and persistent storage, trading a small storage footprint for zero logging complexity.

---

## Hand-Rolled Sorting Algorithm (Due Soon Report)

To satisfy Requirement #5 without using `Collections.sort`, `Arrays.sort`, or `Comparator`, Cascada implements an explicit **Merge Sort** (`TaskDueSoonSorter`).

* **Big-O Analysis:**
    * **Time Complexity:** $O(n \log n)$ across worst, average, and best cases.
    * **Space Complexity:** $O(n)$ auxiliary memory for array merging operations.
* **Why Merge Sort?** Unlike Quick Sort (which degrades to $O(n^2)$ on bad pivot selection) or Insertion Sort ($O(n^2)$ on reverse data), Merge Sort guarantees predictable $O(n \log n)$ performance regardless of initial task order.
* **Sorting Hierarchy:** Primary sort by `dueDate` ascending. Secondary tie-breaker by `priority.weight` descending (using explicit integer weights on the `Priority` enum).

---

## Functional Stream Aggregations

The `TaskAnalyticsService` exposes domain aggregations implemented exclusively with Java Streams (satisfying Requirement #6):

1. `completedTasksPerUserThisWeek()`: Groups tasks by assigned owner and counts completions within a rolling 7-day window using `groupingBy` and `counting`.
2. `overdueTaskCountByPriority()`: Filters tasks by `OVERDUE` status and aggregates total counts per `Priority` level.
3. `averageCompletionTimeInHours()`: Computes average duration between `createdAt` and `completedAt` timestamps using `mapToLong` and `Duration.between`.
4. `cancelledTasksByUser()`: Aggregates cancelled task titles grouped by owner using `groupingBy` and `mapping`.

---

## Concurrency & Thread Safety (Bonus Credit)

### Multi-Threaded Concurrent Reminder Dispatcher
The `ReminderDispatcher` processes pending reminders using an asynchronous worker pool:

* **Thread Pool:** Utilizes `Executors.newFixedThreadPool(4)` to dispatch batch reminders concurrently instead of sequentially.
* **Race Condition Prevention:** Tracks successful dispatch execution using `AtomicInteger` to prevent lost updates across concurrent threads during read-modify-write operations.
* **Resource Cleanup:** Calls `shutdown()` and `awaitTermination(30, TimeUnit.SECONDS)` to ensure clean thread pool termination.

### Race Condition Prevention on Unclaimed Tasks
Cascada supports task broadcasting (`broadcastTask`), creating ownerless tasks visible to all users. To prevent race conditions where multiple users attempt to claim the same task simultaneously, `TaskService.claimTask()` uses explicit method synchronization (`synchronized`), preventing concurrent ownership collisions.

---

## Error Handling & Validation

### Exception Hierarchy
All application exceptions derive from an abstract unchecked base class, `GlobalException` (extending `RuntimeException`). This design allows clean exception propagation within Stream lambda expressions without mandatory checked-exception wrapping.

* `EntityNotFoundException`: Standardized lookup exception taking entity name and identifier.
* `InvalidTaskStateException`: Thrown on illegal state transitions (e.g., postponing a `CANCELLED` task).
* `DuplicateUserException`: Thrown on unique constraint violations during user registration.

### Regex Email Validation
`EmailValidator` is a utility class providing stateless input validation using compiled regular expressions (`^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$`) without external validation frameworks.

---

## Infrastructure & Data Persistence

### Database Engine & Persistence Configuration
Cascada targets an in-memory H2 database using the Hibernate 6 ORM provider.

* Engine Configuration: `jdbc:h2:mem:cascada_db;DB_CLOSE_DELAY=-1` keeps the database active across connections during application runtime.
* Schema Generation: `hibernate.hbm2ddl.auto` is configured to `update` for automated DDL execution upon application boot.

### Universal Repository Contract
Persistence relies on a generic abstraction backed by an in-memory caching mechanism (`TodaysDueTasksCache`):

```java
public interface Repository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    void deleteById(ID id);
}
```

The abstract class JpaRepository<T, ID> implements this interface using direct EntityManager operations (persist, merge, find, createQuery, remove). Concrete repositories (UserRepository, TaskRepository, ReminderRepository) extend JpaRepository to add specific JPQL queries.

## Setup & Running the Application

### Prerequisites

* Java Development Kit (JDK) 26 or higher
* Apache Maven 3.8+

### Building the Project

Clone the repository and compile the project using Maven:
```Bash
mvn clean compile
```

### Running CLI Interactive Session

Run the main application entry point (CascadaApplication):

```Bash
mvn exec:java -Dexec.mainClass="com.cascada.core.CascadaApplication"
```
### NOTE!

Because an in-memory H2 database is used, schema generation and seed data initialization run automatically on startup. Closing the application safely clears the in-memory state.
