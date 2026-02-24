#FROM alpine/git
#WORKDIR /app
#RUN git clone https://github.com/IvanMTD/infosec.git

#FROM maven
#WORKDIR /app
#COPY --from=0 /app/infosec /app
#RUN mvn clean package -DskipTests

#FROM bellsoft/liberica-openjdk-alpine
#EXPOSE 8080 8080
#WORKDIR /app
#COPY --from=1 /app/target/infosec-1.0.0.jar /app
#CMD ["java","-Xms64m","-Xmx900m","-jar","infosec-1.0.0.jar"] OLD
#CMD ["java","-jar","infosec-1.0.0.jar"]

# Этап 1: Клонирование репозитория
#FROM alpine/git AS git-clone
#WORKDIR /app
#RUN git clone https://github.com/IvanMTD/infosec.git

# Этап 2: Сборка приложения с помощью Maven
#FROM maven:3.8.8-eclipse-temurin-17 AS maven-build
#WORKDIR /app
#COPY --from=git-clone /app/infosec /app
#RUN mvn clean package -DskipTests

# Этап 3: Финальный образ с приложением
#FROM eclipse-temurin:17-jre-alpine
#EXPOSE 8080
#WORKDIR /app
#COPY --from=maven-build /app/target/infosec-1.0.0.jar /app
#CMD ["java", "-jar", "infosec-1.0.0.jar"]

FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn ./.mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy AS runner
WORKDIR /app
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]