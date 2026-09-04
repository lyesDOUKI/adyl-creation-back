FROM maven:3.9.16-eclipse-temurin-25 AS builder
WORKDIR /build

# 1. Copie des descripteurs de projet
COPY pom.xml .
COPY libs/pom.xml libs/
COPY libs/spring-web-lib/pom.xml libs/spring-web-lib/
COPY libs/standard-lib/pom.xml libs/standard-lib/
COPY domain/pom.xml domain/
COPY application/pom.xml application/

# 2. Téléchargement des dépendances (mis en cache par Docker)
RUN mvn dependency:go-offline

# 3. Copie du code source complet
COPY libs ./libs
COPY domain ./domain
COPY application ./application

# 4. Compilation
RUN mvn clean package -DskipTests

# --- ÉTAPE RUNTIME ---
FROM eclipse-temurin:25-jre
WORKDIR /app

# On copie le JAR avec son nom fixe (grâce au <finalName> dans le pom.xml)
COPY --from=builder /build/application/target/application.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]