CloudShare Backend

Backend service for the CloudShare platform built using Spring Boot.
This backend provides secure REST APIs for authentication, file management, payment integration, and user credit handling.

 Features

 JWT-based authentication & authorization
 Secure REST APIs
 File upload & sharing support
 Razorpay payment integration
 User credit/subscription management
 MongoDB database integration
 Layered architecture (Controller-Service-Repository)
 Exception handling
 Secure API communication
 Docker support
Tech Stack

Backend

 Java
 Spring Boot
 Spring Security
 JWT Authentication
 Maven

Database

 MongoDB
 Payment Gateway

 Razorpay

Other Tools
 Postman

#  Project Structure
src/main/java
│
├── controller/      # API endpoints
├── service/         # Business logic
├── repository/      # Database interaction
├── model/           # Database models/entities
├── config/          # Security & application configuration
├── dto/             # Request/response DTOs
├── security/        # JWT & authentication logic
├── exception/       # Exception handling
└── CloudShareApplication.java



 Authentication Flow

CloudShare uses JWT-based authentication.
 Flow


User Login
↓
Credentials Verified
↓
JWT Token Generated
↓
Token Sent To Frontend
↓
Frontend Attaches Token In Requests
↓
Spring Security Validates Token
↓
Protected APIs Accessed




API Architecture

The backend follows layered architecture:

Controller Layer
↓
Service Layer
↓
Repository Layer
↓
MongoDB


 Payment Integration

Integrated with Razorpay for:

* subscription handling
* credit purchases
* secure payment verification

## Payment Flow

Frontend Creates Payment Request
↓
Backend Generates Razorpay Order
↓
User Completes Payment
↓
Backend Verifies Signature
↓
Credits Updated In Database


---

#  File Upload System

The backend supports secure file uploads using multipart requests.

## Upload Flow


Frontend Sends Multipart Request
↓
Backend Receives MultipartFile
↓
File Processed
↓
Metadata Stored In MongoDB
↓
Response Returned


#  Key Learnings

Through this project, I gained experience in:

* backend architecture
* secure authentication
* JWT handling
* Spring Security
* REST API development
* payment gateway integration
* MongoDB integration
* debugging full-stack applications

---

#  Future Improvements

* AWS S3 integration
* Redis caching
* Role-based authorization
* File encryption
* Docker Compose
* CI/CD pipeline
* Unit & integration testing
* Rate limiting
* API documentation using Swagger

---

#  API Testing

APIs were tested using:

* Postman
* Frontend integration testing

---

#  Author

Developed by Gaurav

---

frontend link ->https://github.com/gm3919540-sketch/Cloud-Share/edit/main/README.md
