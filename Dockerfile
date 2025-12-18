# Multi-stage Dockerfile pour builder l'application Maven multi-module
# - Build stage: utilise une image Maven + JDK 17 pour builder tout le monorepo
# - Runtime stage: image Java 17 légère qui exécute le JAR du module bff-configuration

# ---- Build stage ----
FROM maven:3.9.3-eclipse-temurin-17 AS builder
WORKDIR /workspace
# Copie tout le repo (le build multi-module nécessite le pom parent)
COPY . /workspace

# Build sans tests pour accélérer la construction en image
RUN mvn -B -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre AS runtime

# Création d'un répertoire pour l'app
WORKDIR /app

# Copie le JAR généré du module bff-configuration (utilise un wildcard pour tolérer le suffixe de version)
COPY --from=builder /workspace/bff-configuration/target/*.jar /app/app.jar

# Installer curl pour le HEALTHCHECK et créer un utilisateur non-root
USER root
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd -r appuser && useradd -r -g appuser appuser \
    && chown -R appuser:appuser /app

# Basculer vers l'utilisateur non-root
USER appuser

# Port exposé par l'application Spring Boot
EXPOSE 8080

# Healthcheck (requiert que l'application expose /actuator/health)
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Commande d'exécution
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
