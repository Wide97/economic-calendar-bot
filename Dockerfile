# Usa un'immagine Maven per buildare il progetto
FROM maven:3.9.3-eclipse-temurin-17 as builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Usa un'immagine Java minimale per eseguire il JAR
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/economic-calendar-bot-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
