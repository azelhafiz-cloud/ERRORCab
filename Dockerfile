# ==============================================================================
# Build Stage: Maven with Java 17
# ==============================================================================
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven POM and source files
COPY pom.xml .
COPY src ./src

# Build the Spring Boot application
# Override compiler properties to ensure Java 17 bytecode compatibility
RUN mvn clean package -DskipTests -Djava.version=17 -Dmaven.compiler.source=17 -Dmaven.compiler.target=17

# ==============================================================================
# Runtime Stage: Java 17 JRE
# ==============================================================================
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the generated Spring Boot executable JAR from the build stage
COPY --from=build /app/target/errorcab-*.jar app.jar

# Render injects the PORT environment variable dynamically at runtime
ENV PORT=8080
EXPOSE 8080

# Run the application, binding to Render's dynamic PORT (fallback to 8080)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
