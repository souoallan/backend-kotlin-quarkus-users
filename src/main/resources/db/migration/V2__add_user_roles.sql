ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE users ADD COLUMN last_login TIMESTAMP WITH TIME ZONE;

CREATE TABLE users_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

INSERT INTO users_roles (user_id, role)
SELECT id, 'USER' FROM users; 