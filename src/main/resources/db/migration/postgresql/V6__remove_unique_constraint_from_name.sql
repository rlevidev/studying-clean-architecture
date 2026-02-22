-- Remove unique constraint from name column in users table
-- Hibernate/JPA might have generated a default name like 'users_name_key'
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_name_key;

-- Also drop any manual unique index if it exists
DROP INDEX IF EXISTS idx_users_name_unique;
