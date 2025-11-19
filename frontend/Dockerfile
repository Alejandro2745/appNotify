# Build de la app con Vite
FROM node:20 AS build
WORKDIR /app

# Copiar solo lo necesario para instalar dep
COPY package*.json ./
RUN npm install

# Copiar el resto del código
COPY . .

# Build de producción
RUN npm run build

# Servir estático con Nginx
FROM nginx:alpine
RUN rm -rf /usr/share/nginx/html/*

COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
