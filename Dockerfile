# Build mərhələsi
FROM maven:3.8.4-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Run mərhələsi
FROM eclipse-temurin:17-jdk-jammy
COPY --from=build /target/*.jar app.jar
EXPOSE 8082

# Bura diqqət: Main klasının tam adını (package ilə birgə) bura yazırıq
ENTRYPOINT ["java", "-cp", "app.jar", "com.muradelmanoglu.Main"]
