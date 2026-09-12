# Architecture

## Update log
- 2026-09-02: Confirmed the working Supabase JWT resource-server flow, including issuer/JWKS validation and the authenticated `/api/v1/me` endpoint returning the current user and role data.
- 2026-09-04: Added disabled-by-default, read-only Google Drive service-account integration for future stream-based Excel readers.
- 2026-09-07: Added the student practice-attempt module with server-side set resolution and historical answer snapshots.
- 2026-09-02: Added the current Flyway-backed schema status to reflect the question hierarchy, MAT/language support, and paper metadata tables now in the database layer.
- 2026-08-30: Added documentation cross-references and clarified the current architecture status and planned module boundaries.
- 2026-08-30: Documented the applied Flyway migration state and confirmed the final auth-boundary and application-schema separation for the initial database setup.
- 2026-08-30: Updated the backend implementation plan to AUTHENTICATION OPTION A: Android handles Supabase Auth; Spring Boot validates the Supabase JWT as a resource server and manages only application/business data.

Status: Implemented foundation, active JWT resource-server access, Student Profile APIs, state/district reference data, Arithmetic, and the independent MAT module.

## Related documentation
- [README.md](../README.md) — project overview and quick start
- [HELP.md](HELP.md) — onboarding guide and setup notes
- [API.md](API.md) — API contract and versioning
- [DATABASE.md](DATABASE.md) — persistence direction and schema planning
- [DEVELOPMENT.md](DEVELOPMENT.md) — setup and developer workflow
- [DECISIONS.md](DECISIONS.md) — architecture rationale and decision log

## Current architecture
The backend currently follows a simple Spring Boot-based modular monolith structure with a minimal, production-ready layout.

### Layers
- API layer: HTTP endpoints and REST responses
- Application layer: service logic and orchestration
- Domain layer: business concepts and validation rules
- Persistence layer: JPA entities and repositories
- Shared configuration: environment, datasource, and general Spring configuration

## Current implementation
- The app is launched from `JnvstGuruBackendApplication`.
- A health endpoint is exposed at `/api/v1/health`.
- Spring Security acts as a JWT resource server validated against the Supabase issuer and JWKS.
- Protected endpoints include `/api/v1/me`, `/api/v1/student-profiles/me`, `/api/v1/student-profiles`, `/api/v1/reference/states`, `/api/v1/reference/states/{stateId}/districts`, and the arithmetic question CRUD routes.
- The project remains intentionally minimal and avoids premature abstractions.
- Arithmetic and Language continue using the generic `questions` architecture. MAT is intentionally independent: `mat_topics` -> `mat_topic_translations` and `mat_questions`, because image-based MAT content has its own identity and storage references.
- Google Drive access is isolated in `GoogleDriveService`; it downloads private files by `fileId` into an `InputStream` and is not connected to persistence or import logic.

## Architectural principles
- Keep modules logically separated.
- Avoid microservice or event-driven complexity at this stage.
- Use DTOs for external API responses.
- Keep database access centralized through repositories.
- Avoid over-engineering for future scale before the real requirements appear.

## Module boundaries
The current structure includes:
- `api` for controllers and request/response DTOs
- `service` for application logic and orchestration
- `domain` for core entities and validation rules
- `repository` for JPA repositories and technical adapters
- `config` for application configuration

The major modules now in active use are auth, student profile, reference data, arithmetic question bank handling, and practice attempts.

## Current constraints
- Authentication is intentionally externalized to Supabase Auth; this backend does not implement password authentication itself.
- The backend is now operating as a JWT Resource Server with protected authenticated endpoints.
- The core domain phase has progressed into Student Profile and arithmetic-question functionality without broadening scope into MAT, passage, or payment domains.
- No event-driven or cloud-specific infrastructure is included beyond the Supabase authentication boundary.
- Google Drive integration is infrastructure-only, read-only, and disabled unless explicitly enabled through deployment configuration.

## Google Drive infrastructure
The optional Google Drive integration uses a service account and the `drive.readonly` scope. Set `GOOGLE_DRIVE_ENABLED=true` and provide the externally managed service-account JSON through `GOOGLE_DRIVE_SERVICE_ACCOUNT_JSON`. The service exposes only `downloadFile(fileId)` and does not write downloaded content to the server filesystem.

## Authentication boundary
JNVST GURU uses Supabase Auth as the identity provider for application users. The backend application database stores only the local application record needed to associate the authenticated account with roles, profile data, subscriptions, and educational content.

The application data model keeps authentication separate from profile and business data:
- `auth.users` is managed by Supabase Auth
- `application.users.auth_user_id` maps the application user record to the Supabase authenticated identity
- `application.users` stores only the application-side status and linkage data
- `application.roles`, `application.user_roles`, `application.student_profiles`, and `application.subscriptions` remain application-domain tables
- `application.users.status` is an application state such as ACTIVE or INACTIVE, not a subscription plan label

This architecture keeps credential handling outside PostgreSQL and preserves a clean separation between identity, profile, and business logic.

## Authentication Option A: Supabase Auth on Android + Spring Boot JWT resource server
This is the approved authentication pattern for the backend implementation.

```text
Android App
    |
    | Supabase Auth
    | phone + password
    v
Supabase Auth (auth.users)
    |
    | JWT access token
    v
Android App
    |
    | Authorization: Bearer <JWT>
    v
Spring Boot Backend
    |
    v
Supabase PostgreSQL
    |
    +-- application.*
```

### Required behavior
1. The Android app communicates directly with Supabase Auth for registration, login, logout, session management, and token refresh.
2. Spring Boot does not implement login/password authentication.
3. Spring Boot does not receive or store user passwords.
4. Spring Boot does not store password, password_hash, or Supabase refresh tokens.
5. The Android app sends the JWT access token to protected backend APIs using the standard header:
   - `Authorization: Bearer <access_token>`
6. Spring Boot validates the Supabase JWT before allowing access to protected APIs.
7. The JWT subject (`sub`) represents the Supabase `auth.users.id`.
8. The backend uses the JWT subject to identify the matching `application.users.auth_user_id` record.
9. If the application user record is missing, the backend provisions it when appropriate after a valid authenticated request.
10. Newly registered normal users receive the `STUDENT` role according to the database design.
11. The backend remains responsible for all application and business logic.

The Android app must not directly access:
- `application.users`
- `application.roles`
- `application.user_roles`
- `application.student_profiles`
- `application.subscription_plans`
- `application.subscriptions`
- future question tables
- future practice tables
- PostgreSQL directly

Only the Spring Boot backend accesses application data.

### Security implementation rules
- Use Spring Security as a JWT Resource Server.
- Extract the Bearer token from the request.
- Validate the token signature.
- Validate the issuer.
- Validate expiration.
- Identify the authenticated Supabase user from `sub`.
- Build an authenticated principal for downstream access checks.
- Enforce authorization based on application roles.
- Do not create a custom JWT generation system.
- Do not generate our own login tokens.
- Do not use the Supabase service-role key in the Android app.
- Keep Supabase service credentials only on the backend if required for server-side operations.

## Database schema boundary
The application schema is managed separately from Supabase's `auth` schema. The backend should not create authentication tables inside the application database beyond the `application.users` record that references `auth.users.id`.

The intended schema layout is:
- `auth` — managed by Supabase Auth
- `application` — managed by Flyway for all application tables

This keeps the system aligned with the provider-owned authentication model and avoids storing credentials in the application database.

## Subscription model boundary
Subscription plans and subscription history are application-domain data, not authentication data.

- `application.subscription_plans` contains plan definitions: `FREE`, `PREMIUM_MONTHLY`, and `PREMIUM_YEARLY`
- `application.subscriptions` stores historical subscription records for a user
- `FREE` is a plan code, not a subscription status value
- `FREE` subscriptions may have `status = ACTIVE` and `end_at = NULL`
- `application.users.is_premium` is not added and should not be used

## Backend implementation plan (next phase)
The implementation phase that follows the completed database foundation is restricted to the following scope.

### In scope
1. Spring Security configuration
2. Supabase JWT validation
3. Authenticated principal handling
4. User provisioning and lookup
5. Role and authority handling
6. `GET /api/v1/me`
7. Student profile APIs
8. Subscription read APIs
9. Global exception handling
10. Validation
11. CORS configuration
12. Health endpoint
13. Secure configuration and environment variables
14. Appropriate tests

### Out of scope for now
- questions
- options
- passages
- translations
- question media
- practice
- mock tests
- progress tracking
- payments
- notifications
- admin question management
- teacher question management

### Database rules for the implementation phase
- Do not modify V1 or V2.
- Do not manually modify the production database.
- Any future database change must use a new Flyway migration.
- Do not create a migration unless the current implementation genuinely requires one.

### Runtime and configuration requirements
Use environment variables for:
- database credentials
- Supabase URL
- JWT issuer/JWKS configuration as required

Never hard-code secrets. Never commit secrets to Git.

### Validation expectations after implementation
- compile the project
- run tests
- verify Flyway sees V1/V2 as already applied
- verify protected APIs reject missing or invalid JWTs
- verify valid Supabase JWT authentication works
- verify the JWT `sub` maps to `application.users.auth_user_id`
- verify `STUDENT`/`TEACHER`/`ADMIN` authorization structure
- verify no passwords or tokens are persisted
- verify no secrets are committed

No code is being deployed as part of this plan update; this is the approved architecture and implementation scope for the next backend work.
