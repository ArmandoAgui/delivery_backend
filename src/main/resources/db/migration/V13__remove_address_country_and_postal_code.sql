alter table addresses
    drop column if exists postal_code,
    drop column if exists country;
