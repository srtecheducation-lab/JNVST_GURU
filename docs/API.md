# API

## Update log
- 2026-09-13: Documented MAT subject/topic practice, latest-review explanations selected by student preferred language, and the current MAT import endpoints.
- 2026-09-02: Documented the working authenticated user flow, Student Profile API, reference data APIs, arithmetic CRUD/filter endpoints, and the current Flyway-backed schema additions.
- 2026-09-02: Added the authenticated subscriptions endpoint to keep the API contract aligned with the implemented controller.
- 2026-09-02: Added read-only question and paper endpoints for reusable question lookup, paper listing, and paper-question occurrences.
- 2026-09-02: Added description of the normalized State/District model and the `stateId` / `districtId` contract for student profiles.
- 2026-09-13: Added the student Language question retrieval endpoint backed by independent per-language question identities.
- 2026-09-04: Added the ADMIN-only Google Drive English Arithmetic import endpoint.
- 2026-09-07: Added authenticated student practice-attempt submission, set status, and latest-attempt review endpoints.
- 2026-09-09: Added student MAT topic/question retrieval and teacher/admin MAT image-question CRUD endpoints.
- 2026-08-30: Added documentation links and refined the API overview section to keep the status and contract notes easier to maintain.

Status: Core foundation, authenticated identity, Student Profile, reference-state/district data, Arithmetic, independent MAT retrieval/import/CRUD, and practice attempts are implemented and validated.

## Related documentation
- [README.md](../README.md) — project overview and quick start
- [HELP.md](HELP.md) — onboarding guide and dev setup
- [ARCHITECTURE.md](ARCHITECTURE.md) — current service structure and principles
- [DATABASE.md](DATABASE.md) — persistence design notes
- [DEVELOPMENT.md](DEVELOPMENT.md) — local workflow and command references
- [DECISIONS.md](DECISIONS.md) — API-related design decisions

### MAT Question Bank
MAT is independent from the generic `questions` API because its content is image-based.

```http
GET    /api/v1/student/mat-topics?language=en
GET    /api/v1/student/mat-questions?topicId=1
GET    /api/v1/mat-questions?topicId=1&active=true
POST   /api/v1/mat-questions
PUT    /api/v1/mat-questions/{id}
DELETE /api/v1/mat-questions/{id}
POST   /api/v1/admin/import/google-drive/mat
```

MAT question requests contain `topicId`, one `questionImageUrl`, four option image URLs, `correctOption` (`A`-`D`), optional `difficulty`, `active`, and `sortOrder`. Student responses never expose `correctOption`; teacher/admin responses do. Image files remain in the configured external file store and only references/URLs are persisted. MAT explanations are not returned by these question endpoints.

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

### Practice attempts
Student-only practice-set endpoints use `practiceMode`, `subject`, and the
subject-specific topic field. `page` is optional and defaults to `0`;
`difficulty` is required for ARITHMETIC and MAT but omitted for LANGUAGE.

```http
POST /api/v1/student/practice-attempts
GET  /api/v1/student/practice-attempts?practiceMode=TOPIC&subject=ARITHMETIC&topic=FRACTION&difficulty=EASY
GET  /api/v1/student/practice-attempts/latest?practiceMode=TOPIC&subject=ARITHMETIC&topic=FRACTION&difficulty=EASY&page=0
GET  /api/v1/student/practice-attempts?practiceMode=SUBJECT&subject=LANGUAGE&language=ENGLISH&page=0
```

Submission example:
```json
{
  "practiceMode": "TOPIC",
  "subject": "ARITHMETIC",
  "topic": "FRACTION",
  "difficulty": "EASY",
  "page": 0,
  "answers": [{"questionId": 36, "selectedOption": "B"}]
}
```

Every question in the resolved 20-question set is stored in a new attempt. Unanswered questions have `selectedOption: null`; `correctOption` is snapshotted in the answer row and is returned only by submission/latest-attempt responses. The status endpoint returns all available sets and exact-set completion flags without requiring one request per set.

MAT supports both practice modes:

```http
POST /api/v1/student/practice-attempts
GET  /api/v1/student/practice-attempts?practiceMode=SUBJECT&subject=MAT&difficulty=EASY
GET  /api/v1/student/practice-attempts?practiceMode=TOPIC&subject=MAT&topicId=1&difficulty=EASY
GET  /api/v1/student/practice-attempts/latest?practiceMode=SUBJECT&subject=MAT&difficulty=EASY&page=0
GET  /api/v1/student/practice-attempts/latest?practiceMode=TOPIC&subject=MAT&topicId=1&difficulty=EASY&page=0
```

For MAT, `topicId` is required only for `TOPIC` mode. Both modes resolve active
`mat_questions` using the requested difficulty and stable `sort_order, id`
ordering. The existing question IDs, image URLs, options, scoring, and answer
identity are unchanged.

The existing `/latest` response includes `explanation` on MAT answer objects.
The value is selected from `mat_question_explanations` using the student's
profile `preferredLanguage`: `bn` selects Bengali, while `en`, null, blank, and
unsupported values select English. There is no cross-language fallback. If the
selected translation does not exist, the nullable field is omitted. Arithmetic
latest responses retain their existing shape.

Language practice is an independent subject and does not accept or send a
difficulty. The persisted attempt therefore has `difficulty: null`.
English and Bengali resolve separate active question sets from
`language_questions`; their IDs are never translated or mapped across
languages:

```http
POST /api/v1/student/practice-attempts
GET  /api/v1/student/practice-attempts/latest?practiceMode=SUBJECT&subject=LANGUAGE&language=ENGLISH&page=0
GET  /api/v1/student/practice-attempts/latest?practiceMode=SUBJECT&subject=LANGUAGE&language=BENGALI&page=0
```

```json
{
  "practiceMode": "SUBJECT",
  "subject": "LANGUAGE",
  "language": "ENGLISH",
  "page": 0,
  "answers": [{"questionId": 101, "selectedOption": "A"}]
}
```

The submission stores every question in the selected language/page set,
including unanswered questions with `selectedOption: null`, and scores against
that independent Language question's `correctOption`.

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
#### Get student practice questions
```http
GET /api/v1/student/arithmetic-questions
Authorization: Bearer <student-access-token>
```

Optional filters and pagination:
```http
GET /api/v1/student/arithmetic-questions?questionType=FRACTION&difficulty=EASY&page=0&size=20
```

The response is paginated and contains only active Arithmetic questions. Each
student question contains `id`, `questionType`, `questionText`, `optionA`,
`optionB`, `optionC`, `optionD`, and `difficulty`. It does not contain
`correctOption`, `explanation`, status, or authoring timestamps.

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

### Google Drive English Arithmetic Import
```http
POST /api/v1/admin/import/google-drive/arithmetic
Authorization: Bearer <supabase-access-token>
Content-Type: application/json
```

Request:
```json
{
  "fileId": "google-drive-file-id",
  "paperId": 1,
  "batchNo": "A001"
}
```

The endpoint requires the `ADMIN` role, reads a private `.xlsx` file through the
Google Drive service, requires `language_code = EN`, and imports all valid rows
transactionally. It reuses questions by normalized English content hash and
creates the corresponding English content and paper occurrence records. New
English content is persisted through its shared `question_id` identity derived
from the reusable question; existing English content is updated in place.

### Google Drive Bengali Arithmetic Import
```http
POST /api/v1/admin/import/google-drive/arithmetic/bengali
Authorization: Bearer <supabase-access-token>
Content-Type: application/json
```

This ADMIN-only endpoint accepts the same request body and Excel structure as
the English importer, but requires every usable row to have `language_code = BN`.
It writes localized prompt and option content to `question_bengali`. When the
same paper, batch, and question number already exist, it reuses that occurrence's
shared question identity; otherwise it reuses an exact normalized Bengali
content hash or creates a new reusable question and paper occurrence.

### Student practice attempts
The existing practice-attempt endpoints support Arithmetic, MAT, and Language.

Arithmetic requests keep the existing `topic` question-type field. MAT requests
use `subject=MAT` and `difficulty=EASY|MEDIUM|HARD`. Subject-wise MAT practice
omits `topicId`; topic-wise MAT practice requires a numeric `topicId` from
`mat_topics.id`:

```http
POST /api/v1/student/practice-attempts
GET /api/v1/student/practice-attempts?practiceMode=SUBJECT&subject=MAT&difficulty=EASY
GET /api/v1/student/practice-attempts?practiceMode=TOPIC&subject=MAT&topicId=1&difficulty=EASY
GET /api/v1/student/practice-attempts/latest?practiceMode=SUBJECT&subject=MAT&difficulty=EASY&page=0
GET /api/v1/student/practice-attempts/latest?practiceMode=TOPIC&subject=MAT&topicId=1&difficulty=EASY&page=0
```

MAT submissions resolve active questions from `mat_questions`, use
`mat_questions.id` and `correct_option`, and store review snapshots in the
existing practice-attempt answer table without using generic `questions`.

## Student Language questions
Language content is selected independently by language and batch. Matching
question numbers across languages do not identify the same question. `size`
controls the number of passages, not the number of questions; each passage
contains its five questions.

```http
GET /api/v1/student/language-questions?language=ENGLISH&page=0&size=1
GET /api/v1/student/language-questions?language=BENGALI&page=0&size=4
```

`batchNo` is not required. The service selects active content for the requested
language, using active questions as the existing active-content convention.
Passages are ordered by `passage_number ASC, id ASC`; questions inside each
passage are ordered by `question_number ASC, id ASC`. The response is
passage-oriented:

```json
{
  "content": [
    {
      "passageId": 1,
      "passageNumber": 1,
      "passageText": "Passage text",
      "questions": [
        {
          "questionId": 101,
          "questionNumber": 1,
          "questionText": "Question text",
          "optionA": "A",
          "optionB": "B",
          "optionC": "C",
          "optionD": "D"
        }
      ]
    }
  ]
}
```

`correctOption` and `explanation` are never returned by this student test-fetch
API. There is no language fallback or difficulty filter. Language submissions
use the existing practice-attempt API with no difficulty and a stored language
code.

### Google Drive Language import
Admins can import available Language CSV files from a batch folder:

```http
POST /api/v1/admin/import/google-drive/language
Content-Type: application/json

{"folderId":"<google-drive-batch-folder-id>"}
```

The batch folder name becomes `batchNo`. Direct children matching positive
passage numbers such as `P001_en.csv`, `P005_bn.csv`, and `P006_en.csv` are
processed; unrelated files and folders are ignored. For recognized passage
numbers, missing English/Bengali counterpart files are reported, but earlier
passages are not required for later imports. Hindi files are ignored and are
not reported as missing. The response reports processed and skipped questions,
failed files, missing expected counterparts, and validation/database errors.
Each CSV is validated and committed independently, so a failed file does not
leave a partial passage or question set.

### Student progress
```http
GET /api/v1/student/progress?recentPage=0&recentLimit=20
```

The authenticated endpoint returns `overall`, all three `subjects`, topic-only
`topics`, and paginated `recentAttempts`. Overall, subject, and topic progress
select only the latest submitted attempt for each unique practice set:

```text
practiceMode + subject + topic + topicId + difficulty
    + languageCode + pageNumber
```

Repeated submissions remain append-only history but do not double-count
progress. `recentAttempts` contains every submission, including repeats, in
`submittedAt DESC, id DESC` order. Accuracy is
`correct / questions * 100`, returned with two decimal places; zero questions
returns `0.00`. Database language codes map `en` to `ENGLISH` and `bn` to
`BENGALI`, with no fallback.

## Planned endpoints
- Subject and topic catalog endpoints
- Mock test endpoints

## Database-only models currently present
The following tables are present in the Flyway-backed schema without public
REST APIs:

- `application.questions`
- legacy question-bank tables retained by migration V18

## API conventions
- Use DTOs instead of exposing JPA entities directly.
- Validate user input with Bean Validation.
- Return consistent JSON error structures.
- Protect endpoints that need an authenticated identity with Supabase-issued JWT access tokens.
- Keep pagination for large result sets.
- Avoid unnecessary fields in responses.
