# CineVerse

## Running on Replit

- Keep the imported Maven/Spring Boot structure; the root `pom.xml` targets Java 17.
- Replit's Java Tools module supplies a compatible JDK and Maven.
- Click **Run** to start the **Start application** workflow.
- Run command: `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=5000`.
- The existing static frontend and `/api/*` routes are served by Spring Boot on port 5000. Open Preview to view the site.
- No external services or secrets are needed for the existing development configuration.

## Data and checks

- The application uses its existing in-memory H2 database. All changes are lost on restart, and sample data is seeded again.
- Run the existing test suite with `mvn test` from the project root.
- Without the workflow's port override, `mvn spring-boot:run` uses port 8080 by default.
