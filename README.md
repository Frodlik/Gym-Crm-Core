# Gym CRM Core
**CRM system for gym management.**

# Prerequisites
Before you build or run the project, make sure the following software is installed on your machine:

| Technology | Minimum Version |
| ---------- |-----------------|
| Java       | 21              |
| Maven      | 3.8+            |
| Docker     | 20.10+          |
| MySQL      | 8.0+            |

Docker is required to run integration tests via Testcontainers.
> *Optional:* You can use MySQL Workbench or any other database client to inspect the schema, test queries, or browse data.


# Getting Started (Local Setup)

1. ## Database Setup (MySQL)
**Before the first run, make sure to create a database and user with proper privileges:**

```sql
CREATE DATABASE gym_crm;
CREATE USER 'gcauser'@'localhost' IDENTIFIED BY 'gcauser';
GRANT ALL PRIVILEGES ON gym_crm.* TO 'gcauser'@'localhost';
```
2. ## Environment Variables
To run the application locally, define the following environment variables in your run configuration.

```
DB_URL=jdbc:mysql://localhost:3306/gym_crm;
DB_USERNAME=gcauser;
DB_PASSWORD=gcauser;
```

