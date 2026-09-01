# Architectural Decisions

## Update log
- 2026-08-30: Added documentation references and clarified the decision log structure so new technical choices are easier to record.
- 2026-08-30: Recorded the final database design decision set and confirmed the successful Flyway foundation migration state for the auth, user, and subscription schema.

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

## BD-008: Supabase Auth as the authentication provider
- Date: 2026-08-30
- Decision: Use Supabase Auth as the authentication provider for JNVST GURU.
- Reason: The project requires an external identity provider and Supabase Auth is the chosen system. This keeps credential handling outside the application database and supports a cleaner separation between identity and business data.
- Alternatives considered: custom password authentication, custom JWT implementation, and OTP-first provider flows. Supabase Auth matches the requirement to use phone + password initially without building an application-owned auth system.
- Consequences: The backend must not implement its own password authentication, must not store password data in application tables, and should keep `application.users` as a mapping record to `auth.users`.

## BD-009: Application schema under `application`
- Date: 2026-08-30
- Decision: Keep all application tables under the `application` PostgreSQL schema and leave Supabase's `auth` schema separate.
- Reason: This preserves the provider-owned authentication boundary while keeping all domain logic in the application-owned schema.
- Alternatives considered: placing application tables in `public` or mixing auth data with domain data. Keeping separate schemas is cleaner and safer for Flyway-managed application migrations.
- Consequences: Flyway should manage the `application` schema only, and the application must reference `auth.users.id` only where needed.

## BD-010: Subscription plan vs. subscription status
- Date: 2026-08-30
- Decision: `FREE`, `PREMIUM_MONTHLY`, and `PREMIUM_YEARLY` are subscription plan values, not `subscriptions.status` values.
- Reason: The product requirement explicitly distinguishes between the plan selected and the subscription lifecycle state. This avoids ambiguous semantics and keeps premium eligibility derived from plan records rather than a boolean field.
- Alternatives considered: storing “premium” as a user flag or using plan names as status values. Those approaches would mix domain concepts and make historical subscription records harder to reason about.
- Consequences: `application.users.status` remains an application status value; `application.subscriptions.status` remains a lifecycle status such as ACTIVE or EXPIRED; `FREE` subscriptions can exist with `status = ACTIVE` and `end_at = NULL`.

## BD-011: Authentication Option A — Supabase Auth on Android + Spring Boot JWT resource server
- Date: 2026-08-30
- Decision: Use Authentication Option A for the backend implementation plan.
- Reason: The Android app owns all Supabase Auth interaction, including registration, login, logout, session management, and token refresh. The backend validates the Supabase JWT as a resource server and manages only application-domain data.
- Alternatives considered: custom backend login, backend-owned password auth, and using the Supabase service-role key in the Android app. All of those violate the approved architecture or security boundary.
- Consequences: Spring Boot must implement JWT resource-server validation and enforce authorization based on the authenticated `sub`, while the Android app remains responsible for Supabase Auth flows and access-token handling. No passwords, refresh tokens, or custom login tokens are stored in the backend.

## Document status legend
- Implemented: Already created and working in the project.
- Planned: Scheduled for a later stage.
- Proposed: Informal idea for future scope.
- Deprecated: Older decisions that are no longer followed.
