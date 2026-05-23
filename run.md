# IssueFlow - Complete Setup and Run Guide

This guide provides **step-by-step instructions** for Windows PowerShell. All commands are tested and ready to use.

---

## Prerequisites

Before starting, ensure you have:

1. **Java 21 or higher** - [Download from Oracle](https://www.oracle.com/java/technologies/downloads/)
   - Verify: `java -version`
   - **Set JAVA_HOME** (if Maven doesn't find Java):
     ```powershell
     # Find your Java installation path
     Get-Command java | Select-Object Source
     
     # Set JAVA_HOME for current PowerShell session
     $env:JAVA_HOME="C:\Program Files\Java\jdk-21"
     
     # Verify it's set
     echo $env:JAVA_HOME
     ```
   - **Permanent Setup** (recommended):
     1. Search Windows for "Environment Variables"
     2. Click "Environment Variables" button
     3. Under "System variables", click "New"
     4. Variable name: `JAVA_HOME`
     5. Variable value: `C:\Program Files\Java\jdk-21` (your actual JDK path)
     6. Click OK and restart PowerShell
   
2. **Docker Desktop for Windows** - [Download here](https://www.docker.com/products/docker-desktop/)
   - Verify: `docker --version`
   - Verify: `docker-compose --version`
---

## Quick Start (5 Steps)

### Step 1: Start PostgreSQL Database

Open **PowerShell** in the project directory and run:

```powershell
docker-compose up -d
```

**Verify** the database is running:

```powershell
docker-compose ps
```

You should see the `issueflow-db` container with status "Up".

---

### Step 2: Install Dependencies and Build

Using Maven wrapper (recommended):

```powershell
.\mvnw.cmd clean install
```

**Note**: First build may take 2-5 minutes to download dependencies.

---

### Step 3: Run the Application

Start the Spring Boot application:

```powershell
.\mvnw.cmd spring-boot:run
```

Wait for the message: `Started IssueFlowApplication in X seconds`

The application is now running at: **http://localhost:8080**

**Keep this terminal open** while using the application.

---

### Step 4: Test the Application

Open a **new PowerShell window** and run:

```powershell
.\mvnw.cmd test
```

**Expected result**: All 31 tests should pass

---

### Step 5: Try the API

Keep the application running from Step 3. In a new PowerShell window, test the API:

```powershell
# Check if server is running
Invoke-RestMethod -Uri http://localhost:8080/users -Method Get
```

You should get an empty array `[]` (no users yet).

---

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

## Sample API Usage (PowerShell)

All commands below use **PowerShell's Invoke-RestMethod**. Make sure the application is running (Step 3).

---

### 1. Register a User

```powershell
$registerBody = @{
    username = "admin"
    email = "admin@example.com"
    password = "password123"
    fullName = "Admin User"
    role = "ADMIN"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/auth/register" `
    -Method Post `
    -Body $registerBody `
    -ContentType "application/json"
```

**Expected Response:**
```json
{
  "id": 1,
  "username": "admin",
  "email": "admin@example.com",
  "fullName": "Admin User",
  "role": "ADMIN"
}
```

---

### 2. Login and Get JWT Token

```powershell
$loginBody = @{
    username = "admin"
    password = "password123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" `
    -Method Post `
    -Body $loginBody `
    -ContentType "application/json"

# Store the token for later use
$token = $loginResponse.accessToken
Write-Host "Token: $token"
```

**Expected Response:**
"eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzc5NTQ5NzU5LCJleHAiOjE3Nzk2MzYxNTl9.AMP_r0ZzRwL6j1na9Wik5QEH2XjiBdCZ1C8II06Qf96SAqvECAXOtj5K2S8sDpXfTCl0NfcLDhmZm5ndSBP6Ng"
```
The `$token` variable now contains your JWT token for authenticated requests.

---

### 3. Create a Project (Authenticated)

```powershell
$projectBody = @{
    name = "My First Project"
    description = "This is a sample project"
    ownerId = 1
} | ConvertTo-Json

$headers = @{
    "Authorization" = "Bearer $token"
}

$project = Invoke-RestMethod -Uri "http://localhost:8080/projects" `
    -Method Post `
    -Body $projectBody `
    -ContentType "application/json" `
    -Headers $headers

$project | ConvertTo-Json 
```

**Expected Response:**
```json
{
  "id": 1,
  "name": "My First Project",
  "description": "This is a sample project",
  "ownerId": 1
  "ownerUsername":  "admin",
  "createdAt":  "2026-05-23T18:23:29.070151",
  "updatedAt":  "2026-05-23T18:23:29.070151"
}
```

### 4. Create a Ticket

```powershell
$ticketBody = @{
    title = "Fix login bug"
    description = "Users cannot login with special characters in password"
    status = "TODO"
    priority = "HIGH"
    type = "BUG"
    projectId = 1
    assigneeId = 1
    dueDate = "2026-06-01T00:00:00Z"
} | ConvertTo-Json

$ticket = Invoke-RestMethod -Uri "http://localhost:8080/tickets" `
    -Method Post `
    -Body $ticketBody `
    -ContentType "application/json" `
    -Headers $headers

$ticket | ConvertTo-Json
```

**Expected Response:**
```json
{
    "id":  1,
    "title":  "Fix login bug",
    "description":  "Users cannot login with special characters in password",
    "status":  "TODO",
    "priority":  "HIGH",
    "type":  "BUG",
    "projectId":  1,
    "assigneeId":  1,
    "assigneeUsername":  "admin",
    "dueDate":  "2026-06-01T00:00:00",
    "isOverdue":  false,
    "createdAt":  "2026-05-23T18:32:02.064368",
    "updatedAt":  "2026-05-23T18:32:02.064368"
}
---

### 5. Add a Comment with Mention

```powershell
$commentBody = @{
    authorId = 1
    content = "Hey @admin, can you take a look at this?"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/tickets/1/comments" `
    -Method Post `
    -Body $commentBody `
    -ContentType "application/json" `
    -Headers $headers
```

---

### 6. Get All Tickets for a Project

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/tickets?projectId=1" `
    -Method Get `
    -Headers $headers
```

---

### 7. Get Current User Profile

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/auth/me" `
    -Method Get `
    -Headers $headers
```

---

### 8. Export Tickets to CSV

```powershell
$csv = Invoke-RestMethod -Uri "http://localhost:8080/tickets/export?projectId=1" `
    -Method Get `
    -Headers $headers

# Save to file
$csv | Out-File -FilePath "tickets_export.csv" -Encoding UTF8
Write-Host "Tickets exported to tickets_export.csv"
```

---

### 9. Get Project Workload

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/projects/1/workload" `
    -Method Get `
    -Headers $headers
```

---

### 10. View Audit Logs

```powershell
# Get all audit logs
Invoke-RestMethod -Uri "http://localhost:8080/audit-logs" `
    -Method Get `
    -Headers $headers

# Filter by entity type
Invoke-RestMethod -Uri "http://localhost:8080/audit-logs?entityType=TICKET" `
    -Method Get `
    -Headers $headers
```

---

## Configuration

The application configuration is in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/issueflow
    username: issueflow
    password: issueflow123
  
jwt:
  secret: your-secret-key-change-this-in-production
  expiration: 86400000  # 24 hours in milliseconds

server:
  port: 8080
```

**Important for Production:**
- Change the JWT secret key
- Use environment variables for sensitive data
- Enable SSL/TLS

---

## Stopping the Application

### Stop Spring Boot Application
In the terminal running the app, press: `Ctrl+C`

### Stop PostgreSQL Container
```powershell
docker-compose down
```

### Stop and Remove All Data (Clean Slate)
```powershell
docker-compose down -v
```

**Warning**: The `-v` flag removes all database data!

---

## Troubleshooting

### Database Connection Issues

**Symptoms**: `Connection refused` or `Could not connect to database`

**Solutions**:
1. Check if PostgreSQL is running:
   ```powershell
   docker-compose ps
   ```

2. View database logs:
   ```powershell
   docker-compose logs postgres
   ```

3. Restart the database:
   ```powershell
   docker-compose restart
   ```

4. Verify database credentials in `application.yml` match `docker-compose.yml`

---

### Port 8080 Already in Use

**Symptoms**: `Port 8080 is already in use`

**Solution 1** - Find and kill the process:
```powershell
# Find process using port 8080
netstat -ano | findstr :8080

# Kill the process (replace PID with actual process ID)
taskkill /PID <PID> /F
```

**Solution 2** - Change the application port:

Edit `src/main/resources/application.yml`:
```yaml
server:
  port: 8081
```

Then access at `http://localhost:8081`

---

### Maven Build Errors

**Symptoms**: Build failures, dependency errors

**Solutions**:
1. Clean the project:
   ```powershell
   .\mvnw.cmd clean
   ```

2. Force update dependencies:
   ```powershell
   .\mvnw.cmd dependency:resolve
   ```

3. Clean and rebuild:
   ```powershell
   .\mvnw.cmd clean install -U
   ```

4. If still failing, delete Maven cache and rebuild:
   ```powershell
   Remove-Item -Recurse -Force "$env:USERPROFILE\.m2\repository"
   .\mvnw.cmd clean install
   ```

---

### Test Failures

**Symptoms**: Tests fail when running `.\mvnw.cmd test`

**Solutions**:
1. Ensure database is running:
   ```powershell
   docker-compose ps
   ```

2. Run tests with verbose output:
   ```powershell
   .\mvnw.cmd test -X
   ```

3. Run a single test:
   ```powershell
   .\mvnw.cmd test -Dtest=TicketServiceTest
   ```

---

### JWT Token Expired

**Symptoms**: `401 Unauthorized` after some time

**Solution**: Login again to get a new token:
```powershell
$loginBody = @{
    username = "admin"
    password = "password123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" `
    -Method Post `
    -Body $loginBody `
    -ContentType "application/json"

$token = $loginResponse.accessToken
```

---

## Database Access

### Connect to PostgreSQL via Docker

```powershell
docker exec -it issueflow-db psql -U issueflow -d issueflow
```

### Useful SQL Commands

```sql
-- List all tables
\dt

-- View users
SELECT * FROM users;

-- View projects
SELECT * FROM projects;

-- View tickets
SELECT * FROM tickets;

-- View audit logs
SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 10;

-- Exit psql
\q
```

### Reset Database (Delete All Data)

```powershell
# Stop containers and remove volumes
docker-compose down -v

# Start fresh
docker-compose up -d

# Run the application (database schema will be recreated)
.\mvnw.cmd spring-boot:run
```

---

## Building JAR File

### Build Production JAR

```powershell
.\mvnw.cmd clean package -DskipTests
```

The JAR will be in `target/issueflow-1.0.0.jar`

### Run the JAR

```powershell
java -jar target/issueflow-1.0.0.jar
```

### Build and Run in One Command

```powershell
.\mvnw.cmd clean package -DskipTests; java -jar target/issueflow-1.0.0.jar
```

---

## Running Tests

### Run All Tests
```powershell
.\mvnw.cmd test
```

### Run Specific Test Class
```powershell
.\mvnw.cmd test -Dtest=TicketServiceTest
```

### Run Specific Test Method
```powershell
.\mvnw.cmd test -Dtest=TicketServiceTest#testCreateTicket
```

### Run Tests with Coverage (if configured)
```powershell
.\mvnw.cmd verify
```

### Skip Tests During Build
```powershell
.\mvnw.cmd clean install -DskipTests
```

---

## Project Structure

```
issueflow/
├── src/
│   ├── main/
│   │   ├── java/com/issueflow/
│   │   │   ├── config/          # Security, JWT config
│   │   │   ├── controller/      # REST endpoints
│   │   │   ├── dto/             # Request/Response objects
│   │   │   ├── entity/          # Database entities
│   │   │   ├── repository/      # JPA repositories
│   │   │   ├── security/        # Authentication
│   │   │   ├── service/         # Business logic
│   │   │   └── IssueFlowApplication.java
│   │   └── resources/
│   │       ├── application.yml  # Configuration
│   │       └── data.sql         # Initial data (if any)
│   └── test/
│       └── java/com/issueflow/  # Unit tests
├── target/                      # Build output
├── docker-compose.yml           # PostgreSQL setup
├── pom.xml                      # Maven dependencies
├── README.md                    # Project overview
└── run.md                       # This file
```

---

## Security Notes

### Default Credentials
- **Database**: `issueflow` / `issueflow123`
- **JWT Secret**: Defined in `application.yml`

### Production Recommendations
1. **Change JWT secret** to a strong random value
2. **Use environment variables** for sensitive data:
   ```yaml
   jwt:
     secret: ${JWT_SECRET}
   ```
3. **Enable HTTPS** with SSL certificates
4. **Use strong database passwords**
5. **Implement rate limiting** for login endpoints
6. **Enable CORS** only for trusted origins

---

## Complete Workflow Example

Here's a complete workflow from scratch:

```powershell
# 1. Start database
docker-compose up -d

# 2. Build and run application
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run

# In a new PowerShell window:

# 3. Create a user
$registerBody = @{
    username = "developer"
    email = "dev@example.com"
    password = "dev123"
    fullName = "Dev User"
    role = "DEVELOPER"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/auth/register" -Method Post -Body $registerBody -ContentType "application/json"

# 4. Login
$loginBody = @{ username = "developer"; password = "dev123" } | ConvertTo-Json
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$token = $loginResponse.accessToken
$headers = @{ "Authorization" = "Bearer $token" }

# 5. Create a project
$projectBody = @{ name = "Web App"; description = "Main web application"; ownerId = 1 } | ConvertTo-Json
$project = Invoke-RestMethod -Uri "http://localhost:8080/projects" -Method Post -Body $projectBody -ContentType "application/json" -Headers $headers

# 6. Create a ticket
$ticketBody = @{
    title = "Implement login page"
    description = "Create responsive login UI"
    status = "TODO"
    priority = "HIGH"
    type = "FEATURE"
    projectId = $project.id
    assigneeId = 1
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/tickets" -Method Post -Body $ticketBody -ContentType "application/json" -Headers $headers

# 7. View tickets
Invoke-RestMethod -Uri "http://localhost:8080/tickets?projectId=$($project.id)" -Method Get -Headers $headers
```

## Quick Reference Commands

| Task | Command |
|------|---------|
| Start database | `docker-compose up -d` |
| Build project | `.\mvnw.cmd clean install` |
| Run application | `.\mvnw.cmd spring-boot:run` |
| Run tests | `.\mvnw.cmd test` |
| Stop database | `docker-compose down` |
| View logs | `docker-compose logs -f` |
| Access database | `docker exec -it issueflow-db psql -U issueflow -d issueflow` |
| Build JAR | `.\mvnw.cmd clean package` |

---
