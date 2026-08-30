# Database

Status: PostgreSQL is the intended production database; the full schema is not yet implemented.

## Related documentation
- [README.md](../README.md) — project overview and quick start
- [HELP.md](HELP.md) — onboarding and daily setup guidance
- [ARCHITECTURE.md](ARCHITECTURE.md) — system layering and design context
- [API.md](API.md) — API-level contract expectations
- [DEVELOPMENT.md](DEVELOPMENT.md) — local database setup and workflow
- [DECISIONS.md](DECISIONS.md) — decision history behind the selected stack

## Current database position
- Primary database: PostgreSQL
- Current scope: foundational configuration only
- Question-bank schema: planned after the foundation is verified

## Local development configuration
The application is configured with PostgreSQL environment-based settings in `application.properties`.

Required environment variables:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

Example:
```bash
export DB_URL=jdbc:postgresql://localhost:5432/jnvst_guru_backend
export DB_USERNAME=jnvst_guru
export DB_PASSWORD=your_secure_password
```

## Design guidelines
- Keep educational content multilingual and translation-friendly.
- Separate language-independent concepts from translated content.
- Keep table relationships explicit and normalized.
- Add indexes for query patterns that become common.
- Favor a clean domain model over database-first shortcuts.

## Planned database work
- Question bank tables and relationships
- Practice set and topic taxonomy
- Student progress tables
- Mock test result tracking
- Performance and indexing review after real usage patterns emerge

## Test strategy
- Automated tests use the H2 in-memory database profile.
- Production configuration remains PostgreSQL-based.
