# API

## Update log
- 2026-09-02: Documented the working authenticated user flow, Student Profile API, reference data APIs, arithmetic CRUD/filter endpoints, and the current Flyway-backed schema additions.
- 2026-09-02: Added the authenticated subscriptions endpoint to keep the API contract aligned with the implemented controller.
- 2026-09-02: Added read-only question and paper endpoints for reusable question lookup, paper listing, and paper-question occurrences.
- 2026-09-02: Added description of the normalized State/District model and the `stateId` / `districtId` contract for student profiles.
- 2026-09-02: Clarified that MAT, Language, and paper tables exist at the schema/database layer, but their public REST APIs remain intentionally out of scope.
- 2026-08-30: Added documentation links and refined the API overview section to keep the status and contract notes easier to maintain.

Status: Core foundation, authenticated identity, Student Profile, reference-state/district data, and the initial Arithmetic Question Bank are implemented and validated. The MAT/language/paper tables are present in Flyway-backed schema support, but their REST endpoints are not yet exposed.

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
### Question
```http
GET /api/v1/questions/{questionId}
```

Returns the reusable question identity without paper-specific fields.

### Papers
```http
GET /api/v1/papers
GET /api/v1/papers/{paperId}
GET /api/v1/papers/{paperId}/questions
```

The paper-question endpoint returns occurrence data including `paperId`, `questionId`, `batchNo`, `questionNumber`, and the paper section `questionType`.

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

### Subscriptions
#### Get current user's subscriptions
```http
GET /api/v1/subscriptions
Authorization: Bearer <supabase-access-token>
```

Returns the authenticated user's subscription history, including plan, lifecycle status, and start/end timestamps.

### Student Profile
#### Get current student profile
```http
GET /api/v1/student-profiles/me
Authorization: Bearer <supabase-access-token>
```

Returns the authenticated student's profile. If a profile does not exist, the caller receives a clean not-found style response.

#### Create student profile
```http
POST /api/v1/student-profiles
Authorization: Bearer <supabase-access-token>
Content-Type: application/json
```

Request body:
```json
{
  "name": "Test Student",
  "dateOfBirth": "2014-05-15",
  "gender": "MALE",
  "category": "GENERAL",
  "residentialArea": "RURAL",
  "classLevel": 6,
  "stateId": 1,
  "districtId": 31,
  "preferredLanguage": "en",
  "examSessionId": 1
}
```

Behavior:
- Returns `201 Created` on successful creation.
- Resolves the application user from the authenticated JWT subject (`sub`).
- Prevents duplicate profiles with `409 Conflict`.
- Validates that `stateId` and `districtId` belong together and returns `400 Bad Request` for invalid combinations.
- Does not accept `userId` or `authUserId` from the request body.

Response example:
```json
{
  "exists": true,
  "id": 2,
  "userId": 1,
  "name": "Test Student",
  "dateOfBirth": "2014-05-15",
  "gender": "MALE",
  "category": "GENERAL",
  "residentialArea": "RURAL",
  "classLevel": 6,
  "stateId": 1,
  "stateName": "Assam",
  "districtId": 31,
  "districtName": "Sribhumi",
  "preferredLanguage": "en",
  "examSessionId": 1,
  "createdAt": "2026-09-01T20:58:01.580367Z",
  "updatedAt": "2026-09-01T20:58:01.580367Z"
}
```

### State and district reference data
#### Get all active states
```http
GET /api/v1/reference/states
```

Response:
```json
[
  {
    "id": 1,
    "code": "AS",
    "name": "Assam"
  }
]
```

#### Get active districts for a state
```http
GET /api/v1/reference/states/{stateId}/districts
```

Example:
```http
GET /api/v1/reference/states/1/districts
```

Response:
```json
[
  {
    "id": 1,
    "code": "BAJALI",
    "name": "Bajali"
  },
  {
    "id": 2,
    "code": "BAKSA",
    "name": "Baksa"
  }
]
```

### Arithmetic Question Bank
#### Get all arithmetic questions
```http
GET /api/v1/arithmetic-questions
```

Filters:
```http
GET /api/v1/arithmetic-questions?questionType=NUMBER_SYSTEM
GET /api/v1/arithmetic-questions?difficulty=EASY
GET /api/v1/arithmetic-questions?status=ACTIVE
```

#### Get arithmetic question by id
```http
GET /api/v1/arithmetic-questions/{id}
```

#### Create arithmetic question
```http
POST /api/v1/arithmetic-questions
Content-Type: application/json
```

Example body:
```json
{
  "questionText": "Which of the following number is the minimum? 80080, 80088, 80880, 80808",
  "questionType": "NUMBER_SYSTEM",
  "optionA": "80080",
  "optionB": "80088",
  "optionC": "80880",
  "optionD": "80808",
  "correctOption": "A",
  "difficulty": "EASY",
  "explanation": null,
  "status": "ACTIVE"
}
```

#### Update arithmetic question
```http
PUT /api/v1/arithmetic-questions/{id}
Content-Type: application/json
```

#### Delete arithmetic question
```http
DELETE /api/v1/arithmetic-questions/{id}
```

This is implemented using a safe status-based soft-delete pattern where appropriate.

## Planned endpoints
- MAT question and language passage/question APIs
- Subject and topic catalog endpoints
- Practice question retrieval endpoints
- Mock test endpoints
- Student progress endpoints

## Database-only models currently present
The following tables are present in the Flyway-backed schema, but their REST APIs are intentionally not implemented yet:

- `application.questions`
- `application.mat_questions`
- `application.language_passages`
- `application.language_questions`
- `application.papers`
- `application.paper_questions`

## API conventions
- Use DTOs instead of exposing JPA entities directly.
- Validate user input with Bean Validation.
- Return consistent JSON error structures.
- Protect endpoints that need an authenticated identity with Supabase-issued JWT access tokens.
- Keep pagination for large result sets.
- Avoid unnecessary fields in responses.
