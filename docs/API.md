# API

## Update log
- 2026-09-02: Documented the working Supabase JWT authentication flow and the authenticated `/api/v1/me` endpoint, which returns the current user and profile context after a valid token is accepted.
- 2026-08-30: Added documentation links and refined the API overview section to keep the status and contract notes easier to maintain.

Status: Foundation API is live and validated for health and authenticated user access. Business-domain endpoints remain planned.

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

### Current user
```http
GET /api/v1/me
Authorization: Bearer <supabase-access-token>
```

Response:
```json
{
  "userId": 1,
  "authUserId": "982050cf-ade9-4c30-b991-530bdea0b920",
  "status": "ACTIVE",
  "roles": ["STUDENT"],
  "studentProfile": null,
  "createdAt": "2026-09-01T20:58:01.580367Z",
  "updatedAt": "2026-09-01T20:58:01.580367Z"
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
- Protect endpoints that need an authenticated identity with Supabase-issued JWT access tokens.
- Keep pagination for large result sets.
- Avoid unnecessary fields in responses.
