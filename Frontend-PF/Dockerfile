# --- Etapa 1: Compilación (Build) ---
# Usamos una imagen de Node.js para instalar dependencias y compilar
FROM node:20-alpine AS builder

WORKDIR /app

# Copiamos los archivos de proyecto e instalamos dependencias
COPY package*.json ./
RUN npm install

# Copiamos todo el código fuente y compilamos la versión de producción
COPY . .
RUN npm run build --prod

RUN ls -R dist
# --- Etapa 2: Servidor (Serve) ---
# Usamos una imagen de Nginx (un servidor web ligero)
FROM nginx:alpine

# Copiamos los archivos compilados de la etapa anterior
# (La carpeta de salida en Angular suele estar en /dist/<nombre-del-proyecto>)
# Revisa tu carpeta "dist" después de compilar si esto da error
COPY --from=builder /app/dist /usr/share/nginx/html
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
# Exponemos el puerto 80 (el puerto por defecto de Nginx)
EXPOSE 80