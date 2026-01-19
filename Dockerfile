FROM maven:3.8.4-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-jammy
COPY --from=build /target/app.jar app.jar
EXPOSE 10000
ENTRYPOINT ["java", "-cp", "app.jar", "com.muradelmanoglu.Main"]
