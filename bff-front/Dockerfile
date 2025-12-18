# Build stage (Debian-based to avoid apk issues on some servers)
FROM node:20-bullseye-slim AS build
WORKDIR /app
# Copier les fichiers de dépendances
COPY package*.json ./
# Ajouter les outils système nécessaires et config npm
ARG NODE_ENV=production
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update \
    && apt-get install -y -qq --no-install-recommends python3 build-essential ca-certificates \
    && npm config set legacy-peer-deps true \
    && npm config set unsafe-perm true \
    && rm -rf /var/lib/apt/lists/*
# Installer les dépendances (inclure dev pour tsc/vite)
RUN NODE_ENV=development npm ci --include=dev --no-audit --progress=false

# Copier le code source
COPY . .
# Build (utilise les binaires locaux via npx)
RUN npx -y tsc -b && npx -y vite build

# Production stage
FROM nginx:alpine
# Copier la configuration nginx pour gérer le fallback SPA et un endpoint /healthz
COPY nginx/nginx.conf /etc/nginx/nginx.conf
# Copier les fichiers buildés par Vite
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
