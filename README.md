# 🏦 Virtual Bank System

A **comprehensive microservices-based Virtual Bank System** developed during my internship at **Ejada Systems Ltd.**  
This project represents a **Backend implementation** of a modern digital banking platform, emphasizing **scalability, security, and resilience** using **cutting-edge technologies** and modern **software architecture**.

---

## 🚀 Project Overview

The **Virtual Bank System** is a distributed application simulating **core banking operations**.  
It follows a **microservices architecture** to ensure **independent scalability**, **fault tolerance**, and **modular development**.  

The system integrates with a **Backend-for-Frontend (BFF)** layer for optimized client-side performance and utilizes the **WSO2 API Gateway** for **API management, security, throttling, and routing**.

This project provided hands-on experience in **designing, developing, and integrating secure distributed systems** within an enterprise-level environment.

---

## 🏗️ System Architecture

```
+----------------+      +-----------------+      +---------------------+
|                |      |                 |      |                     |
| React Frontend |----->|  BFF (Gateway)  |----->| WSO2 API Gateway    |
|                |      | (Spring Boot)   |      | (Security, Routing) |
+----------------+      +-----------------+      +----------+----------+
                                                           |
                                                           |
    +--------------------------+--------------------------+--------------------------+
    |                          |                          |                          |
+---v----------+       +-------v--------+       +---------v---------+       +--------v---------+
|              |       |                |       |                   |       |                  |
| User Service |       | Account Service|       | Transaction Service|       | Logging Service  |
| (Spring Boot)|       |  (Spring Boot) |       |    (Spring Boot)  |       |   (Spring Boot)  |
+--------------+       +----------------+       +-------------------+       +------------------+
        |                      |                          |                          |
+----------------------------------------------------------------------------------------------+
|                                       PostgreSQL Databases                                  |
+----------------------------------------------------------------------------------------------+
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
```

---

## ⭐ Key Features

- **User Management:** Secure registration, authentication, and profile management.  
- **Account Management:** Create and manage savings or checking accounts.  
- **Transaction Processing:** Reliable financial transactions (transfers, deposits, withdrawals).  
- **Centralized Logging:** Asynchronous event logging through **Kafka** for auditing and debugging.  
- **Scheduled Maintenance:** Automated jobs triggered by Kafka for periodic account operations.  
- **API Management & Security:** Managed and secured via **WSO2 API Gateway**.  
- **BFF Layer:** Simplifies frontend integration by aggregating backend responses efficiently.  

---

## 🛠️ Technology Stack

### **Backend**
- **Java 17 / 11** – Core programming language  
- **Spring Boot 2.7 / 3.0** – Microservices framework  
- **Spring Security** – Authentication & authorization  
- **Spring Data JPA** – ORM for PostgreSQL  
- **Spring for Apache Kafka** – Event-driven messaging  

### **API & Integration**
- **WSO2 API Manager** – API gateway for security and throttling  
- **Spring Cloud Gateway (BFF)** – Backend-for-Frontend integration layer  

### **Messaging & Processing**
- **Apache Kafka** – Message broker for asynchronous communication  

### **Database**
- **PostgreSQL** – Relational database (per service)  

### **Others**
- **Maven** – Dependency management and build automation  
- **Docker & Docker Compose** – Containerization for local setup  

---

## 🚦 Getting Started

### **Prerequisites**
- Java 17 or 11  
- Maven 3.6+  
- Node.js & npm  
- Docker & Docker Compose  
- Running WSO2 API Manager instance  

---

### **Installation & Local Development**

#### 1. Clone the repository
```bash
git clone <your-project-repo-link>
cd virtual-bank-system
```

#### 2. Start infrastructure with Docker Compose  
This launches **PostgreSQL**, **Kafka**, **Zookeeper**, and related dependencies.  
```bash
docker-compose up -d
```

#### 3. Run the microservices  
Each service can be started via IDE or terminal.  
```bash
# Navigate to each microservice and run:
mvn spring-boot:run
```
Recommended startup order:
1. `user-service`  
2. `account-service`  
3. `transaction-service`  
4. `logging-service`  
5. `bff-gateway`  

#### 4. Configure & run the WSO2 API Gateway  
- Import the provided API definitions into **WSO2 API Manager**.  
- Deploy the APIs and obtain gateway endpoints.  
- Update the **BFF configuration** to route requests via the WSO2 Gateway URL.  

#### 5. Run the frontend  
```bash
cd frontend
npm install
npm start
```
Access the app at: [http://localhost:3000](http://localhost:3000)

---

## ⚙️ Configuration

Each service uses an `application.yml` file for configuration.  
Important parameters include:
- **Database URLs:** PostgreSQL instance addresses  
- **Kafka Broker:** `localhost:9092`  
- **Service Discovery:** (Optional) Eureka server address  
- **WSO2 Endpoints:** Base URL for gateway  

---

## 🔧 API Endpoints (via WSO2 Gateway)

| Method | Endpoint | Description |
|---------|-----------|-------------|
| POST | `/api/v1/users/register` | Register a new user |
| GET | `/api/v1/accounts/{userId}` | Retrieve all user accounts |
| POST | `/api/v1/transactions/transfer` | Execute a fund transfer |

> 💡 For a complete list of APIs, refer to each service’s **Swagger/OpenAPI documentation** or the **WSO2 Developer Portal**.

---

## 🗄️ Data Flow

1. **Frontend** → sends request to **BFF**  
2. **BFF** → forwards to **WSO2 API Gateway**  
3. **WSO2** → applies security, throttling, and routing rules  
4. **Microservice** → processes the request and emits Kafka events  
5. **Logging Service & Consumers** → asynchronously handle logs and maintenance tasks  

---

## 🙏 Acknowledgments

Special thanks to **Nancy Ibrahim**, my mentor at **Ejada Systems Ltd.**,  
for her continuous support, guidance, and encouragement throughout this project.

---


## 📄 License

This project was developed as part of an internship program at **Ejada Systems Ltd.**  
It is intended for **portfolio and educational purposes only**.

---

**Developed by:**  
🧑‍💻 **Youssef Mohamed Taha**  
🤝 **Abdelrahman Ahmed**

> Looking forward to applying the skills gained from this experience in future projects.  
> Thank you, **Ejada**, for this incredible learning journey!
