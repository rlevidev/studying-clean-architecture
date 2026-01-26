-- V5__fix_refresh_tokens_id_type.sql
ALTER TABLE refresh_tokens ALTER COLUMN id TYPE BIGINT;
