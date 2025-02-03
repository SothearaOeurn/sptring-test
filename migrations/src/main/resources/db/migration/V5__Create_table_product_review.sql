-- Migration to create the `product_reviews` table
CREATE TABLE product_reviews (
    id BIGSERIAL PRIMARY KEY,                   -- Primary key with auto-increment
    user_id BIGINT NOT NULL,                    -- Foreign key to users table
    product_id BIGINT NOT NULL,                 -- Foreign key to products table
    rating INT NOT NULL,                        -- Rating value, required
    review_text TEXT,                           -- Review text, optional
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Created timestamp

    -- Foreign key constraints
    CONSTRAINT fk_product_review_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_product_review_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);