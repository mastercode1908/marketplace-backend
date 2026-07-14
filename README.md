# MegaMart Online Marketplace

## System Overview
**MegaMart** is a robust, modern e-commerce platform designed with a Monolithic (specifically, Modular Monolith) architecture following the Client-Server model. 
It connects buyers and sellers, providing advanced features beyond traditional marketplaces.

- **Architecture:** Modular Monolith (Client-Server).
- **Core Features:** Multi-platform support, comprehensive shopping cart and checkout flows, order management, financial reporting, an integrated AI-powered shopping assistant (RAG Chatbot), and real-time bidirectional communication via WebSockets.
- **Technology Stack:**
  - **Frontend:** React 19 + Vite + Tailwind CSS.
  - **Backend:** Spring Boot 3 + Java 17 + Spring Security (JWT).
  - **Databases:** MySQL (Core System) + PostgreSQL/pgvector (AI Chatbot Engine).

---

## MegaMart Backend API

This repository serves as the core backend of the MegaMart platform. It exposes RESTful APIs for the frontend, enforces complex business logic, handles secure authentication, and interfaces with the underlying databases and external AI services.

### Architecture Design & Principles

The backend deviates from the traditional Layered MVC approach and strictly adopts **Domain-Driven Design (DDD)** packaged as a **Modular Monolith**. This means the codebase is partitioned by business domains rather than technical layers. Each module operates somewhat independently, making the system highly cohesive, loosely coupled, and extremely easy to scale or migrate to true Microservices in the future if required.

Internally, each domain module adheres to a strict N-Tier boundary:
`Controller` (API Entry) -> `Service` (Business Logic) -> `Repository` (Data Access) -> `Entity` (Database Mapping).

### Deep Dive into Modules

- **`identity/`**: The core of security and user management.
  - Handles the complete user lifecycle (Registration, Verification, Profile Updates).
  - Implements **Role-Based Access Control (RBAC)** defining clear boundaries for `Admin`, `Seller`, and `Buyer`.
  - Manages secure **JWT Authentication Flow** and provides seamless integration for **Google OAuth**.
- **`catalog/`**: The product inventory powerhouse.
  - Manages the hierarchical Product and Category systems.
  - Handles the customer Review system, personal Wishlists, and the Product Reporting mechanism (for flagging inappropriate content).
- **`commerce/`**: The transactional heart of the marketplace.
  - Orchestrates the complete Order lifecycle: Cart -> Checkout -> Payment -> Shipping -> Delivery.
  - Generates comprehensive financial and sales reports for sellers and admins.
- **`chatbotRAG/`**: The intelligent assistant engine.
  - Implements **Retrieval-Augmented Generation (RAG)** leveraging **OpenAI's GPT models** and **pgvector**. 
  - Instead of standard text search, it processes natural language user queries, converts them into embeddings, searches the vector database for semantic similarity, and feeds the context to OpenAI to generate highly accurate, contextual product recommendations.
- **`communication/`**: The real-time notification hub.
  - Sets up bidirectional communication using **WebSockets** and **STOMP** protocols.
  - Powers real-time chat between buyers and sellers, and broadcasts instant system notifications (e.g., "Your order has shipped!").
- **`promotion/`**: Handles the logic for managing targeted discounts, vouchers, and promotional campaigns.

### Key Third-Party Integrations
- **Cloudinary:** Robust cloud storage solution for uploading, optimizing, and delivering product images and user avatars.
- **OpenAI API:** Powers the conversational intelligence of the RAG Chatbot.
- **Apache POI & iText:** Libraries used for dynamically generating Excel spreadsheets and PDF documents for robust reporting and data export.

### Comprehensive Setup & Configuration

#### Prerequisites
- **Java 17 (JDK 17)** installed and configured in your `PATH`.
- **Maven 3.8+** (Alternatively, you can use the provided `./mvnw` wrapper).
- **Databases:** Both MySQL and PostgreSQL (with pgvector) must be running. Refer to the `marketplace-database` repository for initialization scripts.

#### 1. Environment Configuration
Create a `.env` file in the root of the project (if using `spring-dotenv`), or configure these variables directly in `src/main/resources/application.properties`:

```properties
# ----------------------------------------
# 1. Core Database Configuration (MySQL)
# ----------------------------------------
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/marketplacesystem
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_mysql_password
SPRING_JPA_HIBERNATE_DDL_AUTO=update

# ----------------------------------------
# 2. Vector Database Configuration (PostgreSQL)
# ----------------------------------------
SPRING_RAG_DATASOURCE_URL=jdbc:postgresql://localhost:5432/marketplace_ai
SPRING_RAG_DATASOURCE_USERNAME=postgres
SPRING_RAG_DATASOURCE_PASSWORD=your_postgres_password

# ----------------------------------------
# 3. Security & Authentication
# ----------------------------------------
JWT_SECRET=your_super_secret_jwt_key_that_is_long_enough
JWT_EXPIRATION_TIME=86400000

# ----------------------------------------
# 4. Third-Party Integrations
# ----------------------------------------
# OpenAI (For RAG Chatbot)
OPENAI_API_KEY=sk-xxxxxxxxxxxxxxx

# Cloudinary (For Media Uploads)
CLOUDINARY_URL=cloudinary://API_KEY:API_SECRET@CLOUD_NAME
```

#### 2. Building the Project
Navigate to the root directory of the backend repository and run the Maven wrapper to download dependencies and build the application:
```bash
./mvnw clean install -DskipTests
```
*(Use `mvnw.cmd` on Windows command prompt).*

#### 3. Running the Application
Once the build is successful, start the Spring Boot server:
```bash
./mvnw spring-boot:run
```
The application will start on the embedded Tomcat server, defaulting to port `8080`. You can access the API at `http://localhost:8080/api/...`.
