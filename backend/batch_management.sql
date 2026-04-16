-- Batch Management System MySQL Setup Script
-- Run this script in MySQL Workbench or CLI before starting backend.

CREATE DATABASE IF NOT EXISTS batch_management;
USE batch_management;

-- Drop tables only if you want a clean reset
DROP TABLE IF EXISTS batches;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'TRAINER') NOT NULL
);

CREATE TABLE batches (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    domain_name VARCHAR(120) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    trainer_id BIGINT NULL,
    progress INT NOT NULL DEFAULT 0,
    status ENUM('ONGOING', 'COMPLETED') NOT NULL DEFAULT 'ONGOING',
    time VARCHAR(50) NOT NULL,
    lab_no VARCHAR(50) NOT NULL,
    no_of_students INT NOT NULL,
    CONSTRAINT fk_batch_trainer
        FOREIGN KEY (trainer_id)
        REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    CONSTRAINT chk_batch_progress
        CHECK (progress >= 0 AND progress <= 100),
    CONSTRAINT chk_batch_dates
        CHECK (end_date >= start_date)
);

-- Default admin user for first login
-- password: admin123 (BCrypt encoded)
INSERT INTO users (name, email, password, role)
VALUES (
    'Default Admin',
    'admin@batch.com',
    '$2a$10$wTdnS9qqwA3L5ioNf6QfHeW6Q32fQ7jC1Y6f8xD8GGjc8QvU.mq6K',
    'ADMIN'
)
ON DUPLICATE KEY UPDATE email = email;
