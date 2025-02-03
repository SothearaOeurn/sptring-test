-- Migration to create the `users` table in the `public` schema
CREATE TABLE public.users (
    id BIGSERIAL PRIMARY KEY,              -- Primary key with auto-increment
    username VARCHAR(255) NOT NULL,        -- Username, cannot be null
    email VARCHAR(255),                    -- Email, optional
    user_id VARCHAR(255),                  -- User ID, optional
    password VARCHAR(255),                 -- Password, optional
    access_token TEXT,                                  -- Access token, optional, supports up to 2000 characters
    expires_in INT,                        -- Access token expiry time, optional
    refresh_expires_in INT,                -- Refresh token expiry time, optional
    refresh_token TEXT,                                      -- Refresh token, optional, supports up to 2000 characters
    token_type VARCHAR(255),               -- Token type, optional
    not_before_policy INT,                 -- Not-before policy, optional
    session_state VARCHAR(255),            -- Session state, optional
    scope VARCHAR(255)                     -- Scope, optional
);
