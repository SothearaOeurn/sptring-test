CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,              -- Primary key with auto-increment
    name VARCHAR(255) NOT NULL,            -- Name of the category, cannot be null
    description TEXT                       -- Description of the category, optional
);