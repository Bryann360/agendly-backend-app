# Agendly (MVP backend) — Kotlin + Spring Boot + Java 21

Este projeto é um **scaffold funcional** para o SaaS multi-tenant tipo *Agendly*.

## Rodar local

### 1) Suba o Postgres
```bash
docker compose up -d
```

### 2) Ajuste o secret do JWT
Edite `src/main/resources/application.yml` e troque `agendly.security.jwt-secret`.

### 3) Rode a aplicação
```bash
./gradlew bootRun
```
> Este repo não inclui Gradle Wrapper. Use `gradle bootRun` se você não tiver `./gradlew`.

## Endpoints principais

### Merchant auth
- `POST /api/merchant/auth/signup`
- `POST /api/merchant/auth/login`

### Merchant (Bearer token)
- `GET /api/merchant/me`
- `GET/PUT /api/merchant/tenant/branding`
- `CRUD /api/merchant/services`
- `CRUD /api/merchant/plans`
- `POST /api/merchant/invites`

### Public (sem token)
- `GET /api/public/{tenantSlug}/branding`
- `GET /api/public/{tenantSlug}/services`
- `GET /api/public/{tenantSlug}/staff`
- `GET /api/public/{tenantSlug}/plans`
- `POST /api/public/{tenantSlug}/client/signup` (via invite)
- `GET /api/public/{tenantSlug}/scheduling/availability`
- `POST /api/public/{tenantSlug}/scheduling/appointments`

## Notas sobre assinatura do cliente
Por padrão, `agendly.billing.allow-free-subscriptions=true` ativa a subscription **sem cobrar** (beta).
Quando integrar Stripe/MP, você troca o fluxo para: `status=PENDING` -> pagamento -> `ACTIVE`.
