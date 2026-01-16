-- Migration: Remove role column from users table
-- This removes the role-based authorization system from the database

-- Drop the index on role column
DROP INDEX IF EXISTS idx_users_role;

-- Remove the role column from users table
ALTER TABLE users DROP COLUMN IF EXISTS role;

-- Drop the user_role type if it exists
DROP TYPE IF EXISTS user_role;
