# HubAccess

Multi-tenant specialty pharma patient access hub case management platform.

## Architecture

- **hub-api/** — Spring Boot 3.3, Java 21, Maven
- **hub-frontend/** — Angular 18 (scaffold only)
- **Database** — PostgreSQL 15 via Supabase with RLS

## Quick Start

```bash
# Copy environment file and fill in values
cp .env.example .env

# Run with Docker Compose (local dev)
docker-compose up -d

# Or run hub-api directly
cd hub-api
./mvnw spring-boot:run
```

## Environment Variables

See `.env.example` for required configuration.

## Deployment

Railway (two services: hub-api, hub-frontend). See `docker-compose.yml` for local dev.
