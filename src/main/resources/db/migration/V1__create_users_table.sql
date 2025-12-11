-- =======================================
--      Migration: Create Users Table
-- =======================================

-- Create user_role enum type
CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role user_role NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes to improve search performance
CREATE UNIQUE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role ON users (role);

-- Comments for documentation
COMMENT ON TABLE users IS 'Stores system user information';
COMMENT ON COLUMN users.id IS 'Unique user identifier';
COMMENT ON COLUMN users.email IS 'User''s email address (must be unique)';
COMMENT ON COLUMN users.name IS 'Username';
COMMENT ON COLUMN users.password_hash IS 'Hash the users password (using BCrypt)';
COMMENT ON COLUMN users.role IS 'User permission on the system (USER or ADMIN)';
COMMENT ON COLUMN users.created_at IS 'Date and time of creation of the register';
COMMENT ON COLUMN users.updated_at IS 'Date and time of last record update';

-- Function to update the updated_at field automatically
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to automatically update the updated_at field
CREATE TRIGGER update_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Insert default admin user (password: admin123)
-- Password must be generated with BCrypt before entering into the bank
-- INSERT INTO users (email, name, password_hash, role) 
-- VALUES ('admin@example.com', 'Administrador', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTiC7FyG/q', 'ADMIN');
