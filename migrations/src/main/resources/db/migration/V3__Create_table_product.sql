-- Migration to create the `products` table
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,                  -- Primary key with auto-increment
    name VARCHAR(255) NOT NULL,                -- Product name, cannot be null
    description TEXT,                          -- Product description, optional
    category_id BIGINT NOT NULL,               -- Foreign key to categories table
    price NUMERIC(10, 2) NOT NULL,             -- Price with precision and scale
    sku VARCHAR(100) NOT NULL UNIQUE,          -- Unique Stock Keeping Unit
    stock_quantity INT NOT NULL DEFAULT 0,     -- Stock quantity, default 0
    reorder_level INT NOT NULL DEFAULT 10,     -- Reorder level, default 10
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Created timestamp
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Updated timestamp
    is_active BOOLEAN NOT NULL DEFAULT TRUE,   -- Active status, default true

    -- Foreign key constraint
    CONSTRAINT fk_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE
);