# IssueFlow - Setup and Run Instructions

## Prerequisites

- **Java 21** or higher
- **Docker** and **Docker Compose**
- **Maven 3.6+** (or use the included Maven wrapper `./mvnw`)

## Setup Instructions

### 1. Start PostgreSQL Database

Start the PostgreSQL database using Docker Compose:

```bash
docker-compose up -d
```

Verify the database is running:

```bash
docker-compose ps
```

### 2. Install Dependencies

Using Maven wrapper (recommended):

```bash
./mvnw clean install
```

Or using system Maven:

```bash
mvn clean install
```

### 3. Run the Application

Using Maven wrapper:

```bash
./mvnw spring-boot:run
```

Or using system Maven:

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 4. Run Tests

Execute all tests:

```bash
./mvnw test
```

Or with system Maven:

```bash
mvn test
```

## API Endpoints

### Authentication

- `POST /auth/register` - Register a new user
- `POST /auth/login` - Login and get JWT token
- `GET /auth/me` - Get current user profile
- `POST /auth/logout` - Logout (invalidate token)

### Users

- `GET /users` - Get all users
- `GET /users/{id}` - Get user by ID
- `PATCH /users/{id}` - Update user
- `DELETE /users/{id}` - Delete user

### Projects

- `POST /projects` - Create a project
- `GET /projects` - Get all projects
- `GET /projects/{id}` - Get project by ID
- `PATCH /projects/{id}` - Update project
- `DELETE /projects/{id}` - Delete project

### Tickets

- `POST /tickets` - Create a ticket
- `GET /tickets?projectId={id}` - Get tickets by project
- `GET /tickets/{id}` - Get ticket by ID
- `PATCH /tickets/{id}` - Update ticket
- `DELETE /tickets/{id}` - Delete ticket

### Comments

- `POST /tickets/{ticketId}/comments` - Add comment to ticket
- `GET /tickets/{ticketId}/comments` - Get all comments for ticket
- `PATCH /tickets/{ticketId}/comments/{id}` - Update comment
- `DELETE /tickets/{ticketId}/comments/{id}` - Delete comment

## Sample API Usage

### 1. Register a User

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@example.com",
    "password": "password123",
    "fullName": "Admin User",
    "role": "ADMIN"
  }'
```

### 2. Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password123"
  }'
```

Response will include a JWT token. Use this token in subsequent requests.

### 3. Create a Project (with JWT token)

```bash
curl -X POST http://localhost:8080/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "My Project",
    "description": "Project description",
    "ownerId": 1
  }'
```

### 4. Create a Ticket

```bash
curl -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "title": "Fix login bug",
    "description": "Users cannot login",
    "status": "TODO",
    "priority": "HIGH",
    "type": "BUG",
    "projectId": 1,
    "assigneeId": 1
  }'
```

## Configuration

The application configuration is in `src/main/resources/application.yml`:

- **Database**: PostgreSQL connection settings
- **JWT**: Secret key and expiration time
- **Server**: Port (default: 8080)

## Stopping the Application

1. Stop the Spring Boot application: `Ctrl+C`
2. Stop the PostgreSQL container: `docker-compose down`

## Troubleshooting

### Database Connection Issues

If you get database connection errors:

1. Ensure PostgreSQL is running: `docker-compose ps`
2. Check database logs: `docker-compose logs postgres`
3. Verify database credentials in `application.yml`

### Port Already in Use

If port 8080 is already in use, change it in `application.yml`:

```yaml
server:
  port: 8081
```

### Maven Build Errors

If you encounter Maven build errors:

1. Clean the project: `./mvnw clean`
2. Update dependencies: `./mvnw dependency:resolve`
3. Rebuild: `./mvnw clean install`

## Database Access

To access the PostgreSQL database directly:

```bash
docker exec -it issueflow-db psql -U issueflow -d issueflow
```

## Building JAR File

To build a deployable JAR file:

```bash
./mvnw clean package
```

The JAR will be in `target/issueflow-1.0.0.jar`

Run the JAR:

```bash
java -jar target/issueflow-1.0.0.jar
```
