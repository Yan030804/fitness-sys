INSERT INTO sys_role (role_code, role_name, description, status)
VALUES ('ROLE_USER', 'Normal User', 'Default system user', 1),
       ('ROLE_ADMIN', 'Administrator', 'System administrator', 1)
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name), description = VALUES(description), status = VALUES(status);

-- BCrypt for 123456
INSERT INTO sys_user (username, password_hash, real_name, nickname, role_id, status)
SELECT 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'System Admin', 'Admin', r.id, 1
FROM sys_role r
WHERE r.role_code = 'ROLE_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'admin');
