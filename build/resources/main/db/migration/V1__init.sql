CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE tenants (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name                VARCHAR(150) NOT NULL,
  slug                VARCHAR(80)  NOT NULL UNIQUE,
  status              VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE'
    CHECK (status IN ('ACTIVE','SUSPENDED','DELETED')),
  created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE tenant_branding (
  tenant_id           UUID PRIMARY KEY REFERENCES tenants(id) ON DELETE CASCADE,
  primary_color       VARCHAR(9)  NOT NULL DEFAULT '#111827',
  secondary_color     VARCHAR(9)  NOT NULL DEFAULT '#F97316',
  logo_key            TEXT,
  cover_key           TEXT,
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE tenant_settings (
  tenant_id                 UUID PRIMARY KEY REFERENCES tenants(id) ON DELETE CASCADE,
  timezone                  VARCHAR(60) NOT NULL DEFAULT 'America/Sao_Paulo',
  currency                  VARCHAR(10) NOT NULL DEFAULT 'BRL',
  locale                    VARCHAR(10) NOT NULL DEFAULT 'pt-BR',
  require_active_subscription_to_book BOOLEAN NOT NULL DEFAULT TRUE,
  booking_window_days       INT NOT NULL DEFAULT 60,
  cancel_min_notice_hours   INT NOT NULL DEFAULT 6,
  max_active_bookings_per_client INT NOT NULL DEFAULT 3,
  updated_at                TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE tenant_domains (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id        UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  domain           VARCHAR(255) NOT NULL UNIQUE,
  verified_at      TIMESTAMPTZ,
  is_primary       BOOLEAN NOT NULL DEFAULT TRUE,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_tenant_domains_tenant_id ON tenant_domains(tenant_id);

CREATE TABLE merchant_users (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id        UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  name            VARCHAR(150) NOT NULL,
  email           VARCHAR(150) NOT NULL,
  password_hash   TEXT NOT NULL,
  role            VARCHAR(30) NOT NULL CHECK (role IN ('OWNER','ADMIN','STAFF')),
  status          VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','BLOCKED')),
  last_login_at   TIMESTAMPTZ,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (tenant_id, email)
);
CREATE INDEX idx_merchant_users_tenant_id ON merchant_users(tenant_id);

CREATE TABLE branches (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id        UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  name            VARCHAR(150) NOT NULL,
  address         JSONB,
  timezone        VARCHAR(60) NOT NULL DEFAULT 'America/Sao_Paulo',
  active          BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_branches_tenant_id ON branches(tenant_id);

CREATE TABLE branch_hours (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  branch_id        UUID NOT NULL REFERENCES branches(id) ON DELETE CASCADE,
  day_of_week      INT NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),
  open_time        TIME NOT NULL,
  close_time       TIME NOT NULL,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (branch_id, day_of_week)
);
CREATE INDEX idx_branch_hours_branch_id ON branch_hours(branch_id);

CREATE TABLE staff_members (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id        UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  branch_id        UUID REFERENCES branches(id) ON DELETE SET NULL,
  name            VARCHAR(150) NOT NULL,
  bio             TEXT,
  phone           VARCHAR(30),
  active          BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_staff_members_tenant_id ON staff_members(tenant_id);
CREATE INDEX idx_staff_members_branch_id ON staff_members(branch_id);

CREATE TABLE staff_working_hours (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  staff_id         UUID NOT NULL REFERENCES staff_members(id) ON DELETE CASCADE,
  day_of_week      INT NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),
  start_time       TIME NOT NULL,
  end_time         TIME NOT NULL,
  branch_id        UUID REFERENCES branches(id) ON DELETE SET NULL,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_staff_working_hours_staff_id ON staff_working_hours(staff_id);

CREATE TABLE services (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id        UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  name            VARCHAR(150) NOT NULL,
  description     TEXT,
  duration_min    INT NOT NULL CHECK (duration_min > 0),
  active          BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_services_tenant_id ON services(tenant_id);

CREATE TABLE staff_services (
  staff_id     UUID NOT NULL REFERENCES staff_members(id) ON DELETE CASCADE,
  service_id   UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
  PRIMARY KEY (staff_id, service_id)
);
CREATE INDEX idx_staff_services_service_id ON staff_services(service_id);

CREATE TABLE plans (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id        UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  name            VARCHAR(120) NOT NULL,
  description     TEXT,
  price_cents     BIGINT NOT NULL CHECK (price_cents >= 0),
  billing_cycle   VARCHAR(20) NOT NULL DEFAULT 'MONTHLY' CHECK (billing_cycle IN ('MONTHLY','YEARLY')),
  trial_days      INT NOT NULL DEFAULT 0 CHECK (trial_days >= 0),
  max_bookings_per_cycle INT,
  cooldown_days   INT NOT NULL DEFAULT 0 CHECK (cooldown_days >= 0),
  active          BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (tenant_id, name)
);
CREATE INDEX idx_plans_tenant_id ON plans(tenant_id);

CREATE TABLE plan_services (
  plan_id     UUID NOT NULL REFERENCES plans(id) ON DELETE CASCADE,
  service_id  UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
  PRIMARY KEY (plan_id, service_id)
);
CREATE INDEX idx_plan_services_service_id ON plan_services(service_id);

CREATE TABLE client_accounts (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name            VARCHAR(150) NOT NULL,
  email           VARCHAR(150),
  phone           VARCHAR(30),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (email)
);

CREATE TABLE tenant_clients (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id            UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  client_account_id    UUID NOT NULL REFERENCES client_accounts(id) ON DELETE CASCADE,
  status               VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','BLOCKED')),
  joined_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
  invited_by_user_id   UUID REFERENCES merchant_users(id) ON DELETE SET NULL,
  UNIQUE (tenant_id, client_account_id)
);
CREATE INDEX idx_tenant_clients_tenant_id ON tenant_clients(tenant_id);
CREATE INDEX idx_tenant_clients_client_id ON tenant_clients(client_account_id);

CREATE TABLE client_invites (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id        UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  created_by_user_id UUID NOT NULL REFERENCES merchant_users(id) ON DELETE RESTRICT,
  token_hash      TEXT NOT NULL UNIQUE,
  expires_at      TIMESTAMPTZ,
  max_uses        INT NOT NULL DEFAULT 1 CHECK (max_uses >= 1),
  used_count      INT NOT NULL DEFAULT 0 CHECK (used_count >= 0),
  preselected_plan_id UUID REFERENCES plans(id) ON DELETE SET NULL,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_client_invites_tenant_id ON client_invites(tenant_id);

CREATE TABLE subscriptions (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id        UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  tenant_client_id UUID NOT NULL REFERENCES tenant_clients(id) ON DELETE CASCADE,
  plan_id          UUID NOT NULL REFERENCES plans(id) ON DELETE RESTRICT,
  status           VARCHAR(30) NOT NULL CHECK (status IN ('ACTIVE','PAST_DUE','CANCELED','EXPIRED','PENDING')),
  started_at       TIMESTAMPTZ,
  current_period_start TIMESTAMPTZ,
  current_period_end   TIMESTAMPTZ,
  cancel_at_period_end BOOLEAN NOT NULL DEFAULT FALSE,
  canceled_at      TIMESTAMPTZ,
  next_billing_at   TIMESTAMPTZ,
  remaining_credits INT,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (tenant_id, tenant_client_id)
);
CREATE INDEX idx_subscriptions_tenant_id ON subscriptions(tenant_id);
CREATE INDEX idx_subscriptions_tenant_client_id ON subscriptions(tenant_client_id);
CREATE INDEX idx_subscriptions_plan_id ON subscriptions(plan_id);

CREATE TABLE subscription_payments (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id        UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  subscription_id  UUID NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
  provider         VARCHAR(50) NOT NULL,
  provider_payment_id VARCHAR(255),
  amount_cents     BIGINT NOT NULL CHECK (amount_cents >= 0),
  platform_fee_cents BIGINT NOT NULL CHECK (platform_fee_cents >= 0),
  merchant_fee_cents BIGINT NOT NULL CHECK (merchant_fee_cents >= 0),
  status           VARCHAR(30) NOT NULL CHECK (status IN ('PENDING','PAID','FAILED','REFUNDED')),
  paid_at          TIMESTAMPTZ,
  failure_reason   TEXT,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_subscription_payments_subscription_id ON subscription_payments(subscription_id);
CREATE INDEX idx_subscription_payments_tenant_id ON subscription_payments(tenant_id);

CREATE TABLE appointments (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id        UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  tenant_client_id UUID NOT NULL REFERENCES tenant_clients(id) ON DELETE RESTRICT,
  staff_id         UUID NOT NULL REFERENCES staff_members(id) ON DELETE RESTRICT,
  branch_id        UUID REFERENCES branches(id) ON DELETE SET NULL,
  service_id       UUID NOT NULL REFERENCES services(id) ON DELETE RESTRICT,
  subscription_id  UUID REFERENCES subscriptions(id) ON DELETE SET NULL,
  start_at         TIMESTAMPTZ NOT NULL,
  end_at           TIMESTAMPTZ NOT NULL,
  status           VARCHAR(30) NOT NULL CHECK (status IN ('CONFIRMED','CANCELED','NO_SHOW','COMPLETED')),
  notes            TEXT,
  cancel_reason    TEXT,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_appointments_tenant_start ON appointments(tenant_id, start_at);
CREATE INDEX idx_appointments_staff_start ON appointments(staff_id, start_at);
CREATE INDEX idx_appointments_tenant_client ON appointments(tenant_client_id);

CREATE TABLE appointment_events (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  appointment_id  UUID NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
  type            VARCHAR(60) NOT NULL,
  payload         JSONB,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_appointment_events_appointment_id ON appointment_events(appointment_id);

CREATE TABLE ledger_entries (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id        UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  direction       VARCHAR(10) NOT NULL CHECK (direction IN ('CREDIT','DEBIT')),
  type            VARCHAR(40) NOT NULL CHECK (type IN ('SUBSCRIPTION_CHARGE','PLATFORM_FEE','PAYOUT','REFUND','ADJUSTMENT')),
  ref_table       VARCHAR(80),
  ref_id          UUID,
  amount_cents    BIGINT NOT NULL CHECK (amount_cents >= 0),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_ledger_entries_tenant_id ON ledger_entries(tenant_id);
CREATE INDEX idx_ledger_entries_ref ON ledger_entries(ref_table, ref_id);

CREATE TABLE payouts (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id        UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  provider         VARCHAR(50) NOT NULL,
  provider_payout_id VARCHAR(255),
  cycle_start      TIMESTAMPTZ NOT NULL,
  cycle_end        TIMESTAMPTZ NOT NULL,
  amount_cents     BIGINT NOT NULL CHECK (amount_cents >= 0),
  status           VARCHAR(30) NOT NULL CHECK (status IN ('PENDING','PROCESSING','PAID','FAILED')),
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  paid_at          TIMESTAMPTZ
);
CREATE INDEX idx_payouts_tenant_id ON payouts(tenant_id);

CREATE TABLE payout_items (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  payout_id        UUID NOT NULL REFERENCES payouts(id) ON DELETE CASCADE,
  subscription_payment_id UUID NOT NULL REFERENCES subscription_payments(id) ON DELETE RESTRICT,
  amount_cents     BIGINT NOT NULL CHECK (amount_cents >= 0)
);
CREATE INDEX idx_payout_items_payout_id ON payout_items(payout_id);

CREATE TABLE platform_plans (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name            VARCHAR(120) NOT NULL UNIQUE,
  price_cents     BIGINT NOT NULL CHECK (price_cents >= 0),
  billing_cycle   VARCHAR(20) NOT NULL DEFAULT 'MONTHLY' CHECK (billing_cycle IN ('MONTHLY','YEARLY')),
  features        JSONB,
  active          BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE tenant_platform_subscriptions (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id        UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE UNIQUE,
  platform_plan_id UUID NOT NULL REFERENCES platform_plans(id) ON DELETE RESTRICT,
  status           VARCHAR(30) NOT NULL CHECK (status IN ('ACTIVE','PAST_DUE','CANCELED','EXPIRED','TRIAL')),
  started_at       TIMESTAMPTZ,
  current_period_end TIMESTAMPTZ,
  provider         VARCHAR(50),
  provider_subscription_id VARCHAR(255),
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
