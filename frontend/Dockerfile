FROM node:20-alpine AS builder
WORKDIR /app

COPY package.json bun.lock ./
RUN npm install

COPY . .
RUN npm run build

FROM nginx:1.27-alpine
WORKDIR /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist ./

EXPOSE 80
