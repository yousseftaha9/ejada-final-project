Virtual Bank System 🏦
A comprehensive, microservices-based Virtual Bank System developed during my internship at Ejada. This project is a full-stack implementation of a modern banking platform, focusing on scalability, security, and robust software architecture using cutting-edge technologies.

🚀 Project Overview
The Virtual Bank System is a distributed application that simulates core banking operations. It is built on a microservices architecture to ensure high availability, independent scalability, and resilience. The system separates concerns into distinct services, managed by an API Gateway and aggregated for client-side efficiency through a Backend-for-Frontend (BFF) layer.

This project provided hands-on, real-world experience in designing, developing, and integrating secure and scalable distributed systems.

🏗️ System Architecture
The architecture follows modern cloud-native principles, consisting of the following key components:


+----------------+      +-----------------+      +---------------------+
|                |      |                 |      |                     |
|   React Frontend|----->|  BFF (Gateway)  |----->| WSO2 API Gateway    |
|                |      | (Spring Boot)   |      | (Security, Routing) |
+----------------+      +-----------------+      +----------+----------+
                                                           |
                                                           |
    +--------------------------+--------------------------+--------------------------+
    |                          |                          |                          |
+---v----------+       +-------v--------+       +---------v---------+       +--------v---------+
|              |       |                |       |                   |       |                  |
| User Service |       | Account Service|       | Transaction Service|       |   Logging Service|
| (Spring Boot)|       |  (Spring Boot) |       |    (Spring Boot)  |       |   (Spring Boot)  |
+--------------+       +----------------+       +-------------------+       +------------------+
        |                      |                          |                          |
        |                      |                          |                          |
+----------------------------------------------------------------------------------------------+
|                                       PostgreSQL Databases                                  |
+----------------------------------------------------------------------------------------------+
        |                      |                          |                          |
        +----------------------+--------------------------+--------------------------+
                                                 |
                                            +----v----+
                                            |         |
                                            |  Apache |
                                            |  Kafka  |
                                            |         |
                                            +----+----+
                                                 |
                                            +----v----+
                                            |         |
                                            | Consumer|
                                            | & Jobs  |
                                            | (Spring)|
                                            +---------+
⭐ Key Features
User Management: Secure user registration, authentication, and profile management.

Account Management: Create, view, and manage different types of bank accounts (e.g., Savings, Checking).

Transaction Processing: Perform secure financial transactions (transfers, deposits, withdrawals) with consistency.

Centralized Logging: All system events, transactions, and errors are asynchronously logged via Kafka for auditing and debugging.

Scheduled Maintenance: Automated, Kafka-triggered jobs for account-related maintenance tasks (e.g., applying interest, archiving old accounts).

API Management & Security: All external traffic is routed through the WSO2 API Gateway for security, rate limiting, and monitoring.

BFF Layer: A dedicated Backend-for-Frontend service to aggregate data from multiple microservices, simplifying the frontend's interaction with the backend.

🛠️ Technology Stack
Backend
Java 17/11: Primary programming language.

Spring Boot 2.7/3.0: Framework for building microservices.

Spring Security: For authentication and authorization.

Spring Data JPA: For database interaction.

Spring for Apache Kafka: For event-driven messaging and logging.

API & Integration
WSO2 API Manager: API gateway for security, throttling, and routing.

Spring Cloud Gateway (BFF): Used as the Backend-for-Frontend layer.

Messaging & Asynchronous Processing
Apache Kafka: Message broker for asynchronous communication and event streaming.

Database
PostgreSQL: Relational database for each microservice (can be per-service or shared schema).

Others
Maven: Dependency management and build automation.

Docker & Docker Compose: For containerization and local development environment setup.

🚦 Getting Started
Prerequisites
Java 17 or 11

Maven 3.6+

Node.js & npm

Docker & Docker Compose

A running WSO2 API Manager instance (or use the provided Docker setup if applicable)

Installation & Local Development
Clone the repository

bash
git clone <your-project-repo-link>
cd virtual-bank-system
Start the Infrastructure with Docker Compose
This will start PostgreSQL, Kafka, Zookeeper, and any other required infrastructure.

bash
docker-compose up -d
Run the Microservices
You can run each Spring Boot microservice from your IDE or the command line.

bash
# Navigate to each service directory and run:
mvn spring-boot:run
Run the services in this recommended order:

user-service

account-service

transaction-service

logging-service

bff-gateway

Configure and Run the WSO2 API Gateway

Import the provided API definitions into your WSO2 API Manager publisher portal.

Deploy the APIs and obtain the gateway endpoints.

Update the bff-gateway configuration to route requests through the WSO2 Gateway URL.

Run the Frontend

bash
cd frontend
npm install
npm start
The application will be available at http://localhost:3000.

Configuration
Each service uses application.yml for configuration. Key configurations include:

Database URLs: Point to your local PostgreSQL instances.

Kafka Broker: localhost:9092

Service Discovery: Eureka server location (if used).

WSO2 Endpoints: The base URL of your WSO2 API Gateway.

🔧 API Endpoints (Via WSO2 Gateway)
All endpoints are secured and accessed through the WSO2 API Gateway. Example endpoints:

POST /api/v1/users/register - Register a new user.

GET /api/v1/accounts/{userId} - Get accounts for a user.

POST /api/v1/transactions/transfer - Initiate a fund transfer.

*Please refer to the individual service Swagger/OpenAPI documentation or the WSO2 Developer Portal for a complete list of available APIs.*

🗄️ Data Flow
A client (React frontend) makes a request to the BFF.

The BFF forwards the request through the WSO2 API Gateway.

The WSO2 Gateway applies security policies (JWT validation, rate limiting) and routes the request to the appropriate microservice.

The microservice (e.g., transaction-service) processes the request, updates its database, and publishes an event (e.g., TRANSACTION_COMPLETED) to a Kafka topic.

The logging-service and kafka-consumer-jobs consume these events to update logs and perform maintenance tasks asynchronously.

🙏 Acknowledgments
A special and heartfelt thanks to Nancy Ibrahim, my mentor and guide at Ejada. Her incredible support, patience, and kindness were instrumental in the successful completion of this project and my learning journey.

🔗 Project Link
GitHub Repository: https://lnkd.in/dfB-y2Nv

📄 License
This project was developed as part of an internship program at Ejada. It is intended for portfolio and educational purposes.

Developed by Youssef Mohamed Taha and Abdelrahman Ahmed
Looking forward to applying these skills in my career ahead. Thank you, Ejada, for this amazing learning opportunity!
