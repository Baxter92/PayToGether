# Build stage
FROM node:20-alpine AS build
WORKDIR /app
# Copier les fichiers de dépendances et installer proprement
COPY package*.json ./
RUN npm install
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
