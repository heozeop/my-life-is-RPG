-- Remove acquired_at column from inventories table
-- Version 3.0 - Clean up redundant timestamp column

-- Drop the acquired_at column from inventories table
-- We now use the standard created_at and updated_at columns
ALTER TABLE inventories DROP COLUMN acquired_at;