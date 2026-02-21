# Multi-stage Dockerfile optimisé pour Java 21
# - Dependencies stage: cache des dépendances Maven pour accélérer les builds
# - Build stage: build du projet multi-module
# - Runtime stage: image Java 21 ultra-légère avec optimisations JVM

# ---- Dependencies stage (cache Maven) ----
FROM maven:3.9.9-eclipse-temurin-21-alpine AS dependencies
WORKDIR /workspace

# Copier uniquement les fichiers POM pour télécharger les dépendances
COPY pom.xml /workspace/
COPY modules/bff/bff-core/pom.xml /workspace/modules/bff/bff-core/
COPY modules/bff/bff-provider/pom.xml /workspace/modules/bff/bff-provider/
COPY modules/bff/bff-wsclient/pom.xml /workspace/modules/bff/bff-wsclient/
COPY modules/bff/bff-api/pom.xml /workspace/modules/bff/bff-api/
COPY modules/bff/bff-configuration/pom.xml /workspace/modules/bff/bff-configuration/

# Créer les répertoires de structure
RUN mkdir -p /workspace/modules/bff/bff-core/src/main/java && \
    mkdir -p /workspace/modules/bff/bff-provider/src/main/java && \
    mkdir -p /workspace/modules/bff/bff-wsclient/src/main/java && \
    mkdir -p /workspace/modules/bff/bff-api/src/main/java && \
    mkdir -p /workspace/modules/bff/bff-configuration/src/main/java

# Télécharger les dépendances (layer cachable)
RUN mvn -B dependency:go-offline -DskipTests

# ---- Build stage ----
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Copier les dépendances depuis le stage précédent
COPY --from=dependencies /root/.m2 /root/.m2

# Copier tout le code source
COPY modules/bff /workspace

# Build avec optimisations
RUN mvn -B -DskipTests clean package \
    && mkdir -p /app \
    && cp /workspace/modules/bff/bff-configuration/target/*.jar /app/app.jar

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine AS runtime

# Métadonnées de l'image
LABEL maintainer="PayToGether Team" \
      version="0.0.1" \
      description="BFF PayToGether - Java 21"

# Créer utilisateur non-root AVANT de copier les fichiers
RUN addgroup -S appuser && adduser -S appuser -G appuser

# Répertoire de l'application
WORKDIR /app

# Copier le JAR depuis le builder
COPY --from=builder --chown=appuser:appuser /app/app.jar /app/app.jar

# Basculer vers l'utilisateur non-root
USER appuser

# Port exposé par l'application Spring Boot
EXPOSE 8080

# Healthcheck optimisé (utilise wget au lieu de curl, natif dans alpine)
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Variables d'environnement pour optimisations JVM
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -XX:+OptimizeStringConcat \
    -Djava.security.egd=file:/dev/./urandom"

# Commande d'exécution avec optimisations
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
