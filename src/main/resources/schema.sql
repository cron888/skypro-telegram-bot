-- Users table (run as postgres user)
-- Connect to the database and create tables
\c telegram_bot_db

CREATE TABLE users (
    id       SERIAL PRIMARY KEY,
    chat_id  BIGINT UNIQUE NOT NULL,
    username VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
