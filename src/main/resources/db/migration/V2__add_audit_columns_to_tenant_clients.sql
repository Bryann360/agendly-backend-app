-- Add auditing columns to tenant_clients to match BaseJpaEntity
ALTER TABLE tenant_clients
  ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now();

