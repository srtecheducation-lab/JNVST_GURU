# Architectural Decisions

Status: Implemented foundational decisions, with future decisions added only when required.

## Related documentation
- [README.md](../README.md) — project overview and quick start
- [HELP.md](HELP.md) — onboarding guide and troubleshooting notes
- [ARCHITECTURE.md](ARCHITECTURE.md) — current modular structure and principles
- [API.md](API.md) — interface-level goals and versioning
- [DATABASE.md](DATABASE.md) — persistence strategy and future schema direction
- [DEVELOPMENT.md](DEVELOPMENT.md) — local environment and release workflow

## BD-001: Java 25 LTS
- Date: 2026-08-30
- Decision: Use Java 25 LTS for the backend.
- Reason: The project explicitly targets Java 25, and the generated application is configured for it.
- Alternatives considered: Java 17 or Java 21. Java 17 would not match the target requirement; Java 25 aligns with the project mandate and current runtime availability.
- Consequences: The build environment must use Java 25; local developer machines need correct `JAVA_HOME` settings.

## BD-002: Spring Boot
- Date: 2026-08-30
- Decision: Use Spring Boot as the application framework.
- Reason: It accelerates backend startup, reduces boilerplate, and aligns with the requested stack.
- Alternatives considered: Plain Spring Framework and other Java frameworks. Spring Boot was selected for its productivity and ecosystem support.
- Consequences: The app follows Spring Boot conventions and uses Maven-driven dependency management.

## BD-003: PostgreSQL
- Date: 2026-08-30
- Decision: Use PostgreSQL as the primary relational database.
- Reason: The project requirements explicitly call for PostgreSQL, and it is well-suited to structured academic and assessment data.
- Alternatives considered: MySQL, SQLite, and NoSQL databases. PostgreSQL was chosen for reliability, data integrity, and future scalability.
- Consequences: Local development requires PostgreSQL, and configuration is externalized via environment variables.

## BD-004: Modular monolith
- Date: 2026-08-30
- Decision: Build a modular monolith rather than a microservice architecture.
- Reason: The project is early-stage and does not need distributed services; a modular monolith keeps boundaries clear while allowing future extraction.
- Alternatives considered: Microservices, serverless, and a single package application. Modular monolith balances simplicity with maintainability.
- Consequences: Modules must remain logically separated, while deployment remains as a single Spring Boot app.

## BD-005: REST API
- Date: 2026-08-30
- Decision: Expose a REST API versioned under `/api/v1`.
- Reason: The Android client requires a clean, versioned REST contract.
- Alternatives considered: GraphQL and custom RPC. REST matches the project requirement and Android client expectations.
- Consequences: Controllers must expose DTOs rather than JPA entities and should use consistent JSON contracts.

## BD-006: Start locally / free
- Date: 2026-08-30
- Decision: Keep the initial setup local and low-cost.
- Reason: The project is in the foundation stage and should remain easy to run and cheap to operate initially.
- Alternatives considered: Cloud-native and infrastructure-heavy options. The chosen approach prioritizes simplicity and local development.
- Consequences: No Redis, Kafka, or cloud-only services are introduced at this stage.

## BD-007: Maven with Maven Wrapper
- Date: 2026-08-30
- Decision: Use Maven with the project-provided Maven Wrapper.
- Reason: This ensures reproducible builds and prevents dependence on a machine-specific Maven installation.
- Alternatives considered: Gradle and globally installed Maven. Maven Wrapper keeps build setup consistent across local environments.
- Consequences: Developers must ensure Java 25 is selected for the runtime used by the wrapper.

## Document status legend
- Implemented: Already created and working in the project.
- Planned: Scheduled for a later stage.
- Proposed: Informal idea for future scope.
- Deprecated: Older decisions that are no longer followed.
