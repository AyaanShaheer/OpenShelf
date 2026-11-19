# -------------------------------
# Stage 1: Frontend build
# -------------------------------
FROM node:22-bookworm-slim AS build

WORKDIR /app

COPY package.json package-lock.json ./

RUN npm ci

COPY . .

# Browser requests use the same origin.
ARG VITE_API_BASE_URL=/api/v1
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

RUN npm run build


# -------------------------------
# Stage 2: Nginx runtime
# -------------------------------
FROM nginxinc/nginx-unprivileged:stable-alpine AS runtime

COPY --from=build \
    --chown=nginx:nginx \
    /app/dist/ \
    /usr/share/nginx/html/

COPY --chown=nginx:nginx \
    nginx/default.conf \
    /etc/nginx/conf.d/default.conf

EXPOSE 8080

USER nginx

CMD ["nginx", "-g", "daemon off;"]