# Build stage
FROM node:20-alpine AS build
WORKDIR /app
# Copier les fichiers de dépendances
COPY package*.json ./
# Utiliser NODE_ENV=development uniquement pour l'installation afin de récupérer les devDependencies (tsc, vite)
ARG NODE_ENV=production
RUN npm config set legacy-peer-deps true
RUN NODE_ENV=development npm ci --include=dev --no-audit --progress=false
# Installer typescript et vite globalement pour garantir la disponibilité de 'tsc' et 'vite' lors du build
RUN npm install -g typescript vite --no-audit --no-progress
# Copier le code source (éviter de copier node_modules du host)
COPY . .
# Build (Vite)
RUN npm run build

# Production stage
FROM nginx:alpine
# Copier la configuration nginx pour gérer le fallback SPA et un endpoint /healthz
COPY nginx/nginx.conf /etc/nginx/nginx.conf
# Copier les fichiers buildés par Vite
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
