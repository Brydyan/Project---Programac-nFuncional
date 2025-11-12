# --- Etapa 1: Compilación (Build) ---
FROM node:20-alpine AS builder

WORKDIR /app

# Copiar archivos de configuración e instalar dependencias
COPY package*.json ./
RUN npm install

# Copiar el resto del proyecto y compilar en modo producción
COPY . .
RUN npm run build

# --- Etapa 2: Servidor (Serve) ---
FROM nginx:alpine

# Copiar archivos compilados desde la etapa anterior
COPY --from=builder /app/dist/Frontend-PF/browser /usr/share/nginx/html

# Copiar la configuración personalizada de Nginx
COPY ./nginx.conf /etc/nginx/conf.d/default.conf

# Exponer el puerto
EXPOSE 80
