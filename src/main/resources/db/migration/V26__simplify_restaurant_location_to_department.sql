ALTER TABLE restaurants
    DROP CONSTRAINT IF EXISTS uk_restaurants_name_city;

ALTER TABLE restaurants
    RENAME COLUMN city TO department;

ALTER TABLE restaurants
    DROP COLUMN IF EXISTS state,
    DROP COLUMN IF EXISTS country;

ALTER TABLE restaurants
    ADD CONSTRAINT uk_restaurants_name_department UNIQUE (name, department);
