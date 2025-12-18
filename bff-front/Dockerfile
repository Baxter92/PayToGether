# Build stage
FROM node:20-alpine AS build
WORKDIR /app
# Copier les fichiers de dépendances
COPY package*.json ./
# Utiliser NODE_ENV=development uniquement pour l'installation afin de récupérer les devDependencies (tsc, vite)
ARG NODE_ENV=production
RUN npm config set legacy-peer-deps true
RUN NODE_ENV=development npm ci --include=dev --no-audit --progress=false
# Installer outils système nécessaires pour compiler d'éventuelles dépendances natives et permettre l'installation (si besoin)
RUN apk add --no-cache python3 build-base libc6-compat \
    && npm config set unsafe-perm true
# Ne pas installer typescript/vite globalement (provoque parfois des erreurs).
# Utiliser les binaires locaux fournis par devDependencies via npx pour la build.

# Copier le code source (éviter de copier node_modules du host)
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
