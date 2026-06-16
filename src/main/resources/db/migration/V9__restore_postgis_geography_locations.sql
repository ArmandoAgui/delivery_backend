CREATE EXTENSION IF NOT EXISTS postgis;

ALTER TABLE addresses
    ADD COLUMN IF NOT EXISTS location GEOGRAPHY(Point, 4326);

UPDATE addresses
SET location = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography
WHERE location IS NULL
  AND latitude IS NOT NULL
  AND longitude IS NOT NULL;

ALTER TABLE addresses
    ALTER COLUMN location SET NOT NULL,
    DROP CONSTRAINT IF EXISTS chk_addresses_latitude_range,
    DROP CONSTRAINT IF EXISTS chk_addresses_longitude_range,
    DROP COLUMN IF EXISTS latitude,
    DROP COLUMN IF EXISTS longitude;

DROP INDEX IF EXISTS idx_addresses_latitude_longitude;
CREATE INDEX IF NOT EXISTS idx_addresses_location_gist ON addresses USING GIST (location);

ALTER TABLE restaurants
    ADD COLUMN IF NOT EXISTS location GEOGRAPHY(Point, 4326);

UPDATE restaurants
SET location = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography
WHERE location IS NULL
  AND latitude IS NOT NULL
  AND longitude IS NOT NULL;

ALTER TABLE restaurants
    ALTER COLUMN location SET NOT NULL,
    DROP CONSTRAINT IF EXISTS chk_restaurants_latitude_range,
    DROP CONSTRAINT IF EXISTS chk_restaurants_longitude_range,
    DROP COLUMN IF EXISTS latitude,
    DROP COLUMN IF EXISTS longitude;

DROP INDEX IF EXISTS idx_restaurants_latitude_longitude;
CREATE INDEX IF NOT EXISTS idx_restaurants_location_gist ON restaurants USING GIST (location);

ALTER TABLE delivery_locations
    ADD COLUMN IF NOT EXISTS location GEOGRAPHY(Point, 4326);

UPDATE delivery_locations
SET location = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography
WHERE location IS NULL
  AND latitude IS NOT NULL
  AND longitude IS NOT NULL;

ALTER TABLE delivery_locations
    ALTER COLUMN location SET NOT NULL,
    DROP CONSTRAINT IF EXISTS chk_delivery_locations_latitude_range,
    DROP CONSTRAINT IF EXISTS chk_delivery_locations_longitude_range,
    DROP COLUMN IF EXISTS latitude,
    DROP COLUMN IF EXISTS longitude;

DROP INDEX IF EXISTS idx_delivery_locations_latitude_longitude;
CREATE INDEX IF NOT EXISTS idx_delivery_locations_location_gist ON delivery_locations USING GIST (location);
