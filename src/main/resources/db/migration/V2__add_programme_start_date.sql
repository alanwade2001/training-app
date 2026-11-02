ALTER TABLE programme_entity
    ADD COLUMN start_date DATE NOT NULL DEFAULT CURRENT_DATE;

ALTER TABLE programme_entity
    ALTER COLUMN start_date DROP DEFAULT;