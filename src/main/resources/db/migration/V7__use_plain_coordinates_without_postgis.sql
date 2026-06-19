-- Keeps local PostgreSQL compatible without requiring PostGIS.
-- V1/V2 now create these columns as DOUBLE PRECISION; this migration fixes databases
-- that already reached V6 with NUMERIC coordinates.

ALTER TABLE addresses
    ALTER COLUMN latitude TYPE DOUBLE PRECISION USING latitude::DOUBLE PRECISION,
    ALTER COLUMN longitude TYPE DOUBLE PRECISION USING longitude::DOUBLE PRECISION;

ALTER TABLE restaurants
    ALTER COLUMN latitude TYPE DOUBLE PRECISION USING latitude::DOUBLE PRECISION,
    ALTER COLUMN longitude TYPE DOUBLE PRECISION USING longitude::DOUBLE PRECISION;

ALTER TABLE delivery_locations
    ALTER COLUMN latitude TYPE DOUBLE PRECISION USING latitude::DOUBLE PRECISION,
    ALTER COLUMN longitude TYPE DOUBLE PRECISION USING longitude::DOUBLE PRECISION;
