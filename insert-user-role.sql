USE bowling;

INSERT INTO role (name) VALUES ('ADMIN'), ('USER')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO users (username, password, email, enabled)
VALUES (
    'admin',
    '$2a$10$nu3H9urEV7NrCsPYx.NPhOVCM3iSaXFxmSUmjLk2IRVbRU1Ud/G1i',
    'admin@bowling.local',
    TRUE
)
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    email = VALUES(email),
    enabled = VALUES(enabled);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN role r ON r.name = 'ADMIN'
WHERE u.username = 'admin'
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);
