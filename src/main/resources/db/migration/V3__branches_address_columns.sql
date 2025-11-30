-- Add typed address columns to branches and backfill from existing JSONB address
ALTER TABLE branches
  ADD COLUMN IF NOT EXISTS address_street TEXT,
  ADD COLUMN IF NOT EXISTS address_number TEXT,
  ADD COLUMN IF NOT EXISTS address_complement TEXT,
  ADD COLUMN IF NOT EXISTS address_neighborhood TEXT,
  ADD COLUMN IF NOT EXISTS address_city TEXT,
  ADD COLUMN IF NOT EXISTS address_state CHAR(2),
  ADD COLUMN IF NOT EXISTS address_zip_code TEXT,
  ADD COLUMN IF NOT EXISTS address_country TEXT DEFAULT 'BR',
  ADD COLUMN IF NOT EXISTS address_extra JSONB;

-- Backfill typed columns from address JSONB, if present
UPDATE branches
SET
  address_street = COALESCE(address->>'street', address_street),
  address_number = COALESCE(address->>'number', address_number),
  address_complement = COALESCE(address->>'complement', address->>'address_complement', address_complement),
  address_neighborhood = COALESCE(address->>'neighborhood', address->>'district', address->>'bairro', address_neighborhood),
  address_city = COALESCE(address->>'city', address_city),
  address_state = COALESCE(address->>'state', address_state),
  address_zip_code = COALESCE(address->>'zipCode', address->>'zip_code', address_zip_code),
  address_country = COALESCE(address->>'country', address_country),
  address_extra = COALESCE(address, address_extra)
WHERE address IS NOT NULL;

-- Indexes for common lookups
CREATE INDEX IF NOT EXISTS idx_branches_address_city ON branches(address_city);
CREATE INDEX IF NOT EXISTS idx_branches_address_state ON branches(address_state);
CREATE INDEX IF NOT EXISTS idx_branches_address_zip_code ON branches(address_zip_code);

-- Note: Do NOT drop the legacy address column yet.

