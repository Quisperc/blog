CREATE DATABASE IF NOT EXISTS demo_blog;
USE demo_blog;
# Drop table t_user;
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    role VARCHAR(20) NOT NULL DEFAULT 'viewer' COMMENT '用户身份',
    register_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    login_time DATETIME NULL COMMENT '登录时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP() COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP() COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='用户表';

-- 为 TiDB 创建索引
# ALTER TABLE t_user ADD INDEX idx_viewer (viewer);
# ALTER TABLE t_user ADD INDEX idx_register_time (register_time);