# Skeleton Backend - Users API

This repository contains the user management API for the Skeleton project.

## Technologies Used

- Quarkus
- Kotlin
- Hibernate ORM with Panache
- PostgreSQL
- Firebase Authentication
- Flyway for migrations

## Environment Setup

### Prerequisites
- JDK 21
- Maven
- Docker (for PostgreSQL)

### Database
The application uses PostgreSQL. Run the container:

```bash
docker run --name skeleton-db-dev -p 5433:5432 -e POSTGRES_USER=skeleton -e POSTGRES_PASSWORD=skeleton -e POSTGRES_DB=skeleton_db -d postgres:16-alpine
```

### Firebase Configuration

For local development, you need to configure authentication with Firebase:

1. Get the JSON credentials file from the Firebase Console:
   - Go to Firebase Console > Project settings > Service accounts
   - Click on "Generate new private key"
   - Save the file as `firebase-credentials.json` in the project root

2. Configure the environment variable (choose one option):

   **Option 1 - Set directly in terminal:**
   ```bash
   export GOOGLE_APPLICATION_CREDENTIALS=$(cat firebase-credentials.json)
   ```

   **Option 2 - Use the helper script:**
   ```bash
   # First run the script to create the file:
   echo 'export GOOGLE_APPLICATION_CREDENTIALS="$(cat firebase-credentials.json)"' > set-firebase-env.sh
   chmod +x set-firebase-env.sh
   
   # Then load the variables:
   source ./set-firebase-env.sh
   ```

## Running the application in dev mode

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

You can run your application in dev mode that enables live coding using:

```bash
./mvnw quarkus:dev
```

To clean the database and apply migrations from scratch:

```bash
./mvnw quarkus:dev -Dquarkus.flyway.clean-at-start=true
```

## API Endpoints

API documentation is available through Swagger UI at:
http://localhost:8080/api/swagger-ui/


> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it's not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/backend-kotlin-quarkus-users-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- Hibernate Validator ([guide](https://quarkus.io/guides/validation)): Validate object properties (field, getter) and method parameters for your beans (REST, CDI, Jakarta Persistence)
- Flyway ([guide](https://quarkus.io/guides/flyway)): Handle your database schema migrations
- SmallRye OpenAPI ([guide](https://quarkus.io/guides/openapi-swaggerui)): Document your REST APIs with OpenAPI - comes with Swagger UI
- Kotlin ([guide](https://quarkus.io/guides/kotlin)): Write your services in Kotlin
- JDBC Driver - PostgreSQL ([guide](https://quarkus.io/guides/datasource)): Connect to the PostgreSQL database via JDBC
- Hibernate ORM with Panache and Kotlin ([guide](https://quarkus.io/guides/hibernate-orm-panache-kotlin)): Define your persistent model in Hibernate ORM with Panache

## Provided Code

### Hibernate ORM

Create your first JPA entity

[Related guide section...](https://quarkus.io/guides/hibernate-orm)


[Related Hibernate with Panache in Kotlin section...](https://quarkus.io/guides/hibernate-orm-panache-kotlin)

### RESTEasy JAX-RS

Easily start your RESTful Web Services

[Related guide section...](https://quarkus.io/guides/getting-started#the-jax-rs-resources)
