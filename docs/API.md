# API

Status: Foundation API complete; no business-domain endpoints added yet.

## Related documentation
- [README.md](../README.md) — project overview and quick start
- [HELP.md](HELP.md) — onboarding guide and dev setup
- [ARCHITECTURE.md](ARCHITECTURE.md) — current service structure and principles
- [DATABASE.md](DATABASE.md) — persistence design notes
- [DEVELOPMENT.md](DEVELOPMENT.md) — local workflow and command references
- [DECISIONS.md](DECISIONS.md) — API-related design decisions

## Versioning
All API paths use the version prefix:

```text
/api/v1
```

## Implemented endpoints
### Health
```http
GET /api/v1/health
```

Response:
```json
{
  "status": "UP",
  "service": "jnvst-guru-backend",
  "timestamp": "2026-08-30T08:00:00Z"
}
```

## Planned endpoints
- Subject and topic catalog endpoints
- Practice question retrieval endpoints
- Mock test endpoints
- Student progress endpoints

## API conventions
- Use DTOs instead of exposing JPA entities directly.
- Validate user input with Bean Validation.
- Return consistent JSON error structures.
- Keep pagination for large result sets.
- Avoid unnecessary fields in responses.
