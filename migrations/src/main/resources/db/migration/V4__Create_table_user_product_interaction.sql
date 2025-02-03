-- Migration to create the `user_product_interactions` table
CREATE TABLE user_product_interactions (
    id BIGSERIAL PRIMARY KEY,                   -- Primary key with auto-increment
    user_id BIGINT NOT NULL,                    -- Foreign key to users table
    product_id BIGINT NOT NULL,                 -- Foreign key to products table
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Created timestamp

    -- Foreign key constraints
    CONSTRAINT fk_user_product_interaction_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_product_interaction_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);