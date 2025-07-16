# Smart Room Allocation System

## 1. Introduction

The Smart Room Allocation System is a Spring Boot application designed to manage and allocate rooms efficiently. It provides functionalities for users to find, book, and manage room reservations, while administrators can oversee rooms, equipment, and user accounts.

### Core Features:
*   **User Authentication & Authorization:** Secure login and registration for users, with role-based access control (e.g., ADMIN, LECTURER). JWT (JSON Web Tokens) are used for securing API endpoints.
*   **Room Management:**
    *   Administrators can create, view, and update room details (e.g., room number, name, capacity, type, status).
    *   Users can view available rooms and their details.
*   **Booking Management:**
    *   Authenticated users can create bookings for available rooms.
    *   Users can view their own bookings.
    * Users can *update their own future bookings*,with validation to present changes to past or ongoing bookings
    *   Users can cancel their bookings.
    *   Functionality to view upcoming bookings for a specific room.
    * *Enhanced Email notifications*:Automated emails for booking confirmations,cancellations,providing clear details(including room number/name)
*   **Equipment(Learning Resource) Management:**
    *   Administrators can add and manage equipment associated with rooms.
    *   Users can view equipment available in rooms.
*   **User Management:**
    *   Administrators can manage user accounts, including updating *some user details(supporting partial details)
    *   User data returned via API endpoints is handled securely using Data Transfer Objects (DTOs) to prevent exposure of sensitive information like passwords.
*   **AdminDashboard Analytics:**
    *   A dedicated API endpoint (`/api/admin/dashboard/stats`) provides aggregated system statistics for administrators, including:
          *  TotalBookings ever made
          * Total available rooms.
          * Total active rooms.
          * Total upcoming bookings.
          * Total active users.
    * **API Consistency:** Standardized JSON response format (`Status`,`Message`,`Data`) across various endpoints for better predictability and ease of integration
*   **Notifications:** Email notifications for booking confirmations and cancellations.

**Note:** This system includes gamification features (like points and leaderboards) which are not covered in this setup guide and can be disregarded for basic operational setup.

## 2. Prerequisites

Before you begin, ensure you have the following installed:
*   **Java Development Kit (JDK):** Version 17 or higher.
*   **Maven:** Apache Maven 3.6.x or higher (for building the project).
*   **Relational Database:** A running instance of a relational database like PostgreSQL, MySQL, or H2. The application is configured to use a generic JDBC connection, so ensure you have the appropriate JDBC driver.
*   **IDE (Optional but Recommended):** An Integrated Development Environment like IntelliJ IDEA or Eclipse.

## 3. Initial Setup

### 3.1. Clone the Repository

```bash
git clone <repository-url>
cd smart-room-allocation
```

### 3.2. Configure Application Properties

The main configuration file is `src/main/resources/application.properties`. You will need to update it with your specific settings.

**a. Database Configuration:**

Update the following properties for your database connection:

```properties
spring.datasource.url=jdbc:yourdatabase://localhost:5432/yourdbname # Example for PostgreSQL
spring.datasource.username=yourdbuser
spring.datasource.password=yourdbpassword
spring.datasource.driver-class-name=org.postgresql.Driver # Example for PostgreSQL

spring.jpa.hibernate.ddl-auto=update # Or 'create' for initial setup, 'validate', or 'none'
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect # Example for PostgreSQL
```

*   Replace `jdbc:yourdatabase://localhost:5432/yourdbname` with your database's JDBC URL.
*   Set `yourdbuser` and `yourdbpassword` to your database credentials.
*   Update `spring.datasource.driver-class-name` and `spring.jpa.properties.hibernate.dialect` according to your chosen database (e.g., `com.mysql.cj.jdbc.Driver` and `org.hibernate.dialect.MySQLDialect` for MySQL).

**b. JWT Configuration:**

Set a strong secret key and an appropriate expiration time for JWT tokens:

```properties
jwt.secret=YOUR_VERY_STRONG_AND_SECRET_KEY_REPLACE_ME
jwt.expiration=86400 # Token expiration time in seconds (e.g., 24 hours)
```

**c. Email Configuration (for Notifications):**

Configure your SMTP server details for sending email notifications:

```properties
spring.mail.host=smtp.example.com
spring.mail.port=587
spring.mail.username=your-email@example.com
spring.mail.password=your-email-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### 3.3. Build the Application

Navigate to the project's root directory (where `pom.xml` is located) and run:

```bash
mvn clean install
```

## 4. Running the Application

Once the build is successful, you can run the application using:

```bash
mvn spring-boot:run
```

Alternatively, you can run the packaged JAR file:

```bash
java -jar target/smart-room-allocation-0.0.1-SNAPSHOT.jar
```

The application will typically start on `http://localhost:8080` (or the port configured in `application.properties` via `server.port`).

## 5. Initial User Registration

The application allows public registration via the `/api/users/register` endpoint. You can use a tool like Postman or `curl` to register the first user, who can then be potentially promoted to an ADMIN role directly in the database if needed for initial setup and further user management.

---

This guide should help you get the Smart Room Allocation system up and running. For API endpoint details, refer to the controller classes within the `src/main/java/com/smartroom/allocation/controller` package.