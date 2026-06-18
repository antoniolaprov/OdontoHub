# ============================================================
#  OdontoHub — Frontend (React + Vite) servido por Nginx
#  Build multi-stage: Node compila a SPA; Nginx serve o estático.
# ============================================================

# ---- Estágio 1: build da SPA ----
FROM node:22-alpine AS build
WORKDIR /app

# Instala dependências primeiro (camada cacheável enquanto o lock não muda)
COPY package.json package-lock.json ./
RUN npm ci

# Copia o código e gera o bundle de produção (dist/)
COPY . .
RUN npm run build

# ---- Estágio 2: runtime (Nginx) ----
FROM nginx:alpine

# Configuração que serve a SPA e faz proxy de /api para o backend
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Estático gerado no build
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
