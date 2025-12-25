# Task Manager App

A Spring Boot RESTful API for managing tasks, featuring CRUD operations, pagination, validation, and robust unit/integration testing. Uses H2 in-memory database and Spring Data JDBC for persistence.

## Features
- Create, read, update, and delete tasks
- Pagination for task listing
- Input validation and error handling
- In-memory H2 database (no persistent storage required)
- Unit and integration tests

## Technologies Used
- Java 21
- Spring Boot 3.x
- Spring Data JDBC
- H2 Database
- Maven
- JUnit 5 & Mockito

## Project Structure

```
src/
  main/
    java/com/example/taskmanagerapp/
      controllers/
      dto/
      enums/
      exceptions/
      mapper/
      models/
      repositories/
      services/
      util/
    resources/
      application.properties
      schema.sql
  test/
    java/com/example/taskmanagerapp/
      controllers/
      integrationtests/
      services/
```

## Getting Started

### Prerequisites
- Java 21 or higher
- Maven 3.8 or higher

### Setup & Run

1. **Build the project:**
   ```sh
   mvn clean install
   ```

2. **Run the application:**
   ```sh
   mvn spring-boot:run
   ```
   The app will start on [http://localhost:9091](http://localhost:9091).

3. **API Endpoints:**
   - `POST /api/v1/tasks` - Create a new task
   - `GET /api/v1/tasks/{id}` - Get task by ID
   - `PUT /api/v1/tasks/{id}` - Update a task
   - `DELETE /api/v1/tasks/{id}` - Delete a task
   - `GET /api/v1/tasks/page/{page}/size/{size}` - Get paginated tasks

### Database
- Uses H2 in-memory database by default.
- Schema is initialized from `schema.sql` on startup.
- No persistent data after app shutdown.

### Running Tests

- **Unit and Integration Tests:**
  ```sh
  mvn test
  ```
- Tests are located under `src/test/java/com/example/taskmanagerapp/`.
- Integration tests start the full Spring context and use the same in-memory database (no data persists between runs).
- Unit tests use mocks and do not require the application context to start.

### Troubleshooting
- If you encounter port conflicts, kill task runing on port 9091 or change the server port in `src/main/resources/application.properties`:
  ```
  server.port=<your desired port number ex : 9090>
  ```
- For H2 console access, visit [http://localhost:9091/h2-console](http://localhost:9091/h2-console).
- Click on connect button 


