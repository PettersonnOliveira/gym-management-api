# 🏋️ Gym Management API

REST API developed to manage a gym: member registration, workout registration and enrollment management. It allows enrolling members in workouts, canceling enrollments and checking enrollment history.

This is the fourth project in my Java and Spring Boot study portfolio. Unlike the previous ones, the focus here was not to learn new concepts but to gain speed and autonomy by applying the same architecture (Controller → Service → Repository → DTO) independently in a different domain.

💡 Problem it solves

Gyms that manage enrollments manually face problems such as:

- Allowing a member to enroll twice in the same workout
- Losing track of who is enrolled in which workout
- Lack of a centralized registry of members and workouts

The Gym Management API solves this by centralizing members, workouts and enrollment control, with business rules that prevent invalid operations.

🛠️ Technologies used

- Java 21
- Spring Boot
- Spring Web (REST API)
- Spring Data JPA
- MySQL
- Bean Validation (input validation)
- Maven

🏗️ Architecture

Controller → Service → Repository → Database

Controller: receives HTTP requests and returns responses
Service: contains business rules, including duplicate-check validation
Repository: communicates with the database, including derived queries (existsBy)
DTOs: protect the API, controlling exactly what goes in and out — including nested DTOs for relationships
Exceptions: custom exceptions are handled globally to provide consistent error responses

📦 Data model

Member
Field	Type
id	Long
name	String
email	String
phone	String

Workout
Field	Type
id	Long
name	String
description	String
duration	Integer (minutes)

Enrollment
Field	Type
id	Long
enrollmentDate	LocalDateTime
member	Member (@ManyToOne relationship)
workout	Workout (@ManyToOne relationship)

🧠 Relationships

A Member can be enrolled in many Workouts via multiple Enrollments
A Workout can have many Members enrolled via multiple Enrollments
Enrollment is the join entity that solves the N:N relationship and stores the enrollment date

📌 Available endpoints

Members
Action	Method	Route
Create member	POST	/members
List members	GET	/members
Get member by ID	GET	/members/{id}
Update member	PUT	/members/{id}
Delete member	DELETE	/members/{id}

Workouts
Action	Method	Route
Create workout	POST	/workouts
List workouts	GET	/workouts
Get workout by ID	GET	/workouts/{id}
Update workout	PUT	/workouts/{id}
Delete workout	DELETE	/workouts/{id}

Enrollments
Action	Method	Route
Enroll member in workout	POST	/enrollments
List enrollments	GET	/enrollments
Cancel enrollment	DELETE	/enrollments/{id}

Example — Enroll a member in a workout

POST /enrollments

```json
{
  "memberId": 1,
  "workoutId": 1
}
```

Response (201 Created)

```json
{
  "id": 1,
  "enrollmentDate": "2026-07-22T17:58:12.345678",
  "member": {
    "id": 1,
    "name": "Lucas Ferreira",
    "email": "lucas.ferreira@email.com",
    "phone": "11999998888"
  },
  "workout": {
    "id": 1,
    "name": "Strength Workout - Full Body",
    "description": "Workout focused on strength with free weights",
    "duration": 60
  }
}
```

🔥 Business rules

Rule	Behavior
Member already enrolled in the same workout cannot enroll again	400 Bad Request
Member not found	404 Not Found
Workout not found	404 Not Found

⚠️ Error handling

The API uses custom exceptions handled globally via @RestControllerAdvice:

ResourceNotFoundException → returns 404 Not Found (resource not found by ID)
BusinessRuleException → returns 400 Bad Request (business rule violation, such as duplicate enrollment)

✅ Validations

Input DTOs use Bean Validation:

name, email → @NotBlank
memberId, workoutId → @NotNull

Requests that violate these validations automatically return 400 Bad Request.

⚙️ How to run the project locally

Prerequisites
- Java 21 installed
- MySQL running locally
- Maven (or use the included mvnw)

Steps
Clone the repository

```bash
git clone <repository-url>
```

Configure the database in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/gym_management?createDatabaseIfNotExist=true
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
spring.jpa.hibernate.ddl-auto=update
```

⚠️ This file contains sensitive credentials and should not be committed with real data in a production project.

Run the application:

```bash
./mvnw spring-boot:run
```

The API will be available at http://localhost:8080

Testing

We recommend using Postman or Insomnia to test the endpoints.

🗺️ Next steps (roadmap)

- Authentication and authorization with Spring Security + JWT
- Scheduling and workout availability
- Member attendance history (check-in)

📝 What I learned building this project

This project was a deliberate practice exercise: applying, without help, the same architecture used in the previous three projects in a new domain. This brought gains in:

- Speed in structuring Entity, Repository, DTOs, Service and Controller independently
- Faster recognition of mechanical errors (misconfigured route, incompatible type, forgotten annotation) without relearning the underlying concept
- Confidence in replicating the join-entity pattern (Enrollment) to solve an N:N relationship, already on the second try with less guidance
- Reinforcement of custom exception handling (ResourceNotFoundException, BusinessRuleException) and the criteria to choose which to use in each situation

Developed by Petterson Oliveira as part of my Java and Spring Boot study portfolio.

