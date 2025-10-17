# ============================================
# ETAPA 1: COMPILACIÓN
# ============================================
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY src ./src

RUN ./mvnw clean package -DskipTests -B


# ============================================
# ETAPA 2: EJECUCIÓN
# ============================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]