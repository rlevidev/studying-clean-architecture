-- Alter column role from user_role enum type to VARCHAR for portability
ALTER TABLE users ALTER COLUMN role TYPE VARCHAR(20);

-- Update existing values to match enum names
UPDATE users SET role = 'USER' WHERE role = 'user';
UPDATE users SET role = 'ADMIN' WHERE role = 'admin';
