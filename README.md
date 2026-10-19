# Library Management System

A RESTful Library Management System built with Spring Boot and MySQL, supporting book catalog management, member registration, and a full borrow/return workflow with business-rule enforcement.

## Tech Stack

- Java 17
- Spring Boot (Spring Web, Spring Data JPA, Spring Validation)
- Hibernate / JPA
- MySQL
- Maven

## Features

- Full CRUD for books and members
- Borrow/return workflow with automatic due-date calculation (14-day loan period)
- Real-time tracking of available copies per book
- Business rule enforcement: a book cannot be borrowed if no copies are available
- Borrow history tracking per member
- Centralized exception handling with correct HTTP status codes (400, 404, 409)
- Request validation (required fields, valid email format, minimum copy counts)

## Data Model

**Book**
- id, title, author, isbn, totalCopies, availableCopies

**Member**
- id, name, email, phone

**BorrowRecord** (association entity linking Book and Member)
- id, book, member, borrowDate, dueDate, returnDate, status (BORROWED / RETURNED / OVERDUE)

A book has a one-to-many relationship with borrow records, as does a member. This models borrowing as its own entity with attributes (dates, status) rather than a plain many-to-many link, reflecting how the real-world transaction works.

## API Endpoints

### Books
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/books | List all books |
| GET | /api/books/{id} | Get a single book |
| POST | /api/books | Add a new book |
| PUT | /api/books/{id} | Update a book |
| DELETE | /api/books/{id} | Remove a book |

### Members
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/members | List all members |
| GET | /api/members/{id} | Get a single member |
| POST | /api/members | Register a new member |
| DELETE | /api/members/{id} | Remove a member |

### Borrowing
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/borrow | Borrow a book (body: `{bookId, memberId}`) |
| POST | /api/return/{borrowRecordId} | Return a borrowed book |
| GET | /api/borrow-records | List all borrow records |
| GET | /api/borrow-records/member/{memberId} | List a member's borrow history |

## Business Rules

1. A book can only be borrowed if `availableCopies > 0`.
2. Borrowing decrements `availableCopies` by 1 and creates a `BorrowRecord` with status `BORROWED`, a `borrowDate` of today, and a `dueDate` 14 days later.
3. Returning a book sets `returnDate` to today, updates status to `RETURNED`, and increments `availableCopies` by 1.
4. A book that is already marked `RETURNED` cannot be returned again.

## Setup

1. Create a MySQL database:
```sql
   CREATE DATABASE librarydb;
```
2. Configure `src/main/resources/application.properties` with your MySQL username/password.
3. Run the application:./mvnw spring-boot:run
4. The API will be available at `http://localhost:8080`.
## Example Requests

**Create a book**
```
POST /api/books
{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "9780132350884",
  "totalCopies": 3
}
```

**Borrow a book**
```
POST /api/borrow
{
  "bookId": 1,
  "memberId": 1
}
```

**Return a book**
```
POST /api/return/1
```