# 1) Build de la app con Vite
FROM node:20 AS build
WORKDIR /app

# Copiamos solo lo necesario para instalar deps (aprovecha cache)
COPY package*.json ./
RUN npm install

# Ahora copiamos el resto del código
COPY . .

# Build de producción (usa el script "build": "vite build")
RUN npm run build

# 2) Servir estático con Nginx
FROM nginx:alpine
RUN rm -rf /usr/share/nginx/html/*

COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
