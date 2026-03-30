FROM node:20-alpine AS builder
WORKDIR /app
COPY hub-frontend/package*.json ./
RUN npm ci --legacy-peer-deps
COPY hub-frontend/ .
RUN npm run build -- --configuration production

FROM nginx:alpine
COPY --from=builder /app/dist/hub-frontend/browser /usr/share/nginx/html
COPY hub-frontend/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
