# IssueFlow - Ticket Management Backend Platform

A RESTful backend API for managing projects, tickets, and comments with JWT-based authentication.

## Features

### Core Features
- **User Management** - Register, update, and manage users with role-based access (ADMIN, DEVELOPER)
- **JWT Authentication** - Secure stateless authentication with JWT tokens (accessToken, tokenType, expiresIn)
- **Project Management** - Create and manage projects with ownership
- **Ticket Management** - Create, update, and track tickets with status workflow, dueDate, and isOverdue tracking
- **Comment System** - Add and manage comments on tickets
- **Input Validation** - Comprehensive validation on all API endpoints
- **Error Handling** - Consistent error responses with detailed messages
- **Optimistic Locking** - Prevent concurrent update conflicts

### Extended Features
- **Audit Log** - Track all state changes (CREATE, UPDATE, DELETE, RESTORE) with actor and timestamp
- **Ticket Dependencies** - Manage blocking relationships between tickets
- **File Attachments** - Upload and manage file attachments on tickets (max 10MB)
- **CSV Export/Import** - Bulk ticket operations via CSV files
- **Soft Delete** - Soft delete projects and tickets with restore capability (ADMIN only)
- **@Mentions** - Parse and track @username mentions in comments with notifications
- **Workload Tracking** - View developer workload by project
- **Auto-Escalation** - Automatic priority escalation for overdue tickets (hourly scheduler)
- **Auto-Assignment** - Automatically assign tickets to least-loaded developers

### Business Rules
- **Status Workflow**: TODO → IN_PROGRESS → IN_REVIEW → DONE (no backward transitions)
- **DONE Tickets**: Cannot be modified once marked as DONE
- **Concurrent Updates**: Optimistic locking prevents simultaneous edits
- **Validation**: All inputs validated before processing

## 📊 Project Statistics

- **Total Files**: 120+
- **Lines of Code**: ~8,000+
- **Test Coverage**: Core and extended features
- **API Endpoints**: 40+
- **Database Tables**: 9
- **Extended Features**: 9/9 (100% Complete)

## Extended Features Highlights

### Audit Log System
Every create, update, delete, and restore action is logged with:
- Action type (CREATE, UPDATE, DELETE, RESTORE)
- Entity type (USER, PROJECT, TICKET, COMMENT, etc.)
- Actor (USER or SYSTEM for automated actions)
- Timestamp and user who performed the action

### Auto-Escalation Scheduler
- Runs hourly to check overdue tickets
- Escalates priority: LOW → MEDIUM → HIGH → CRITICAL
- Sets `isOverdue` flag automatically
- Logs all escalations with `actor: SYSTEM`

### Auto-Assignment
- When no assignee specified, automatically assigns to least-loaded developer
- Uses real-time workload calculation (open tickets per developer)
- Logged as `actor: SYSTEM` in audit trail

### Soft Delete
- Projects and tickets are soft-deleted (not permanently removed)
- `deletedAt` timestamp tracks when deleted
- ADMIN users can restore via `/restore` endpoints
- All queries filter out soft-deleted items by default

### @Mentions System
- Parses `@username` in comments
- Validates mentioned users exist
- Stores mentions for notifications
- `/users/{userId}/mentions` API for viewing mentions (paginated)

### File Attachments
- Upload files up to 10MB
- Stores in `uploads/` directory with UUID filenames
- Tracks filename, content type, size, uploader

### CSV Import/Export
- Export all tickets for a project to CSV
- Import tickets in bulk with validation
- Returns detailed error report for failed imports

## Technology Stack

- **Java 21**
- **Spring Boot 3.3.0**
- **Spring Security** with JWT
- **Spring Data JPA**
- **PostgreSQL 16**
- **Lombok**
- **Bean Validation**
- **JUnit 5 & Mockito**

## Project Structure

```
issueflow/
├── src/main/java/com/issueflow/
│   ├── config/          # Security configuration
│   ├── controller/      # REST API endpoints
│   ├── dto/             # Request/Response objects
│   ├── entity/          # JPA entities
│   ├── exception/       # Custom exceptions & handlers
│   ├── repository/      # Data access layer
│   ├── security/        # JWT & authentication
│   └── service/         # Business logic
├── src/main/resources/
│   └── application.yml  # Configuration
└── src/test/            # Test files
```

## Quick Start

See [run.md](run.md) for detailed setup instructions.

### Prerequisites
- Java 21+
- Docker & Docker Compose
- Maven 3.6+

### Start Database
```bash
docker-compose up -d
```

### Run Application
```bash
./mvnw spring-boot:run
```

### Run Tests
```bash
./mvnw test
```

## API Documentation

### Authentication

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/login` | Login and get JWT | No |
| GET | `/auth/me` | Get current user | Yes |
| POST | `/auth/logout` | Logout | Yes |

### Users

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/users` | Create new user | No |
| GET | `/users` | Get all users | Yes |
| GET | `/users/{userId}` | Get user by ID | Yes |
| POST | `/users/update/{userId}` | Update user | Yes |
| DELETE | `/users/{userId}` | Delete user | Yes |

### Projects

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/projects` | Create project | Yes |
| GET | `/projects` | Get all projects | Yes |
| GET | `/projects/{id}` | Get project by ID | Yes |
| PATCH | `/projects/{id}` | Update project | Yes |
| DELETE | `/projects/{id}` | Delete project | Yes |

### Tickets

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/tickets` | Create ticket | Yes |
| GET | `/tickets?projectId={id}` | Get tickets by project | Yes |
| GET | `/tickets/{id}` | Get ticket by ID | Yes |
| PATCH | `/tickets/{id}` | Update ticket | Yes |
| DELETE | `/tickets/{id}` | Delete ticket | Yes |

### Comments

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/tickets/{ticketId}/comments` | Add comment to ticket | Yes |
| GET | `/tickets/{ticketId}/comments` | Get all comments for ticket | Yes |
| PATCH | `/tickets/{ticketId}/comments/{id}` | Update comment | Yes |
| DELETE | `/tickets/{ticketId}/comments/{id}` | Delete comment | Yes |

### Audit Logs

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/audit-logs` | Get audit logs with filters | Yes |

### Dependencies

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/tickets/{ticketId}/dependencies` | Add blocking dependency | Yes |
| GET | `/tickets/{ticketId}/dependencies` | List dependencies | Yes |
| DELETE | `/tickets/{ticketId}/dependencies/{blockerId}` | Remove dependency | Yes |

### Attachments

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/tickets/{ticketId}/attachments` | Upload file attachment | Yes |
| DELETE | `/tickets/{ticketId}/attachments/{id}` | Delete attachment | Yes |

### CSV Operations

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/tickets/export?projectId={id}` | Export tickets to CSV | Yes |
| POST | `/tickets/import` | Import tickets from CSV | Yes |

### Soft Delete

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/projects/deleted` | List deleted projects | Yes |
| POST | `/projects/{id}/restore` | Restore project | Yes |
| GET | `/tickets/deleted?projectId={id}` | List deleted tickets | Yes |
| POST | `/tickets/{id}/restore` | Restore ticket | Yes |

### Mentions & Workload

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/users/{userId}/mentions` | Get user mentions (paginated) | Yes |
| GET | `/projects/{projectId}/workload` | Get developer workload | Yes |

## Authentication

All protected endpoints require a JWT token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

Get a token by logging in:

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password123"}'
```

## Database Schema

### Tables
- **users** - User accounts with roles
- **projects** - Project definitions
- **tickets** - Work items/issues
- **comments** - Ticket discussions

### Relationships
- User → Projects (One-to-Many as owner)
- Project → Tickets (One-to-Many)
- User → Tickets (One-to-Many as assignee)
- Ticket → Comments (One-to-Many)
- User → Comments (One-to-Many as author)

## Testing

The project includes:
- **Unit Tests**: Service layer business logic
- **Integration Tests**: Controller + Service + Repository
- **Security Tests**: Authentication & authorization

Run all tests:
```bash
./mvnw test
```

## AI Usage Documentation

See [prompts.md](prompts.md) for detailed documentation on AI agent usage during development.

**Model Used**: Claude 3.5 Sonnet (Anthropic)

**Key AI Contributions**:
- Architecture design and best practices
- Complete code implementation
- Security configuration
- Error handling strategy
- Test implementation
- Documentation

## Configuration

Main configuration in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/issueflow
    username: issueflow
    password: issueflow123

jwt:
  secret: <your-secret-key>
  expiration: 86400000  # 24 hours
```

## Troubleshooting

See [run.md](run.md) for common issues and solutions.

## License

This project is created for educational purposes as part of TDP 2026 Home Assignment.

## Author

Developed with assistance from Claude 3.5 Sonnet AI agent.
