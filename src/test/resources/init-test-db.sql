CREATE DATABASE IF NOT EXISTS gym_crm_test;
USE gym_crm_test;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    isActive BOOLEAN NOT NULL
    );

CREATE TABLE IF NOT EXISTS training_types (
     id BIGINT AUTO_INCREMENT PRIMARY KEY,
     training_type_name VARCHAR(100) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS trainees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_of_birth DATE,
    address VARCHAR(200),
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_trainees_user_id FOREIGN KEY (user_id) REFERENCES users(id)
    );

CREATE TABLE IF NOT EXISTS trainers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    specialization BIGINT,
    CONSTRAINT fk_trainers_user_id FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_trainers_specialization FOREIGN KEY (specialization) REFERENCES training_types(id)
    );

CREATE TABLE IF NOT EXISTS trainings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    training_name VARCHAR(100) NOT NULL,
    training_date DATE NOT NULL,
    training_duration INT NOT NULL,
    trainee_id BIGINT NOT NULL,
    trainer_id BIGINT NOT NULL,
    training_type_id BIGINT NOT NULL,
    CONSTRAINT fk_trainings_trainee_id FOREIGN KEY (trainee_id) REFERENCES trainees(id),
    CONSTRAINT fk_trainings_trainer_id FOREIGN KEY (trainer_id) REFERENCES trainers(id),
    CONSTRAINT fk_trainings_training_type_id FOREIGN KEY (training_type_id) REFERENCES training_types(id)
    );

CREATE TABLE IF NOT EXISTS trainees_trainers (
    trainee_id BIGINT NOT NULL,
    trainer_id BIGINT NOT NULL,
    PRIMARY KEY (trainee_id, trainer_id),
    CONSTRAINT fk_trainees_trainers_trainee_id FOREIGN KEY (trainee_id) REFERENCES trainees(id),
    CONSTRAINT fk_trainees_trainers_trainer_id FOREIGN KEY (trainer_id) REFERENCES trainers(id)
    );

INSERT INTO training_types (training_type_name) VALUES
    ('Cardio'),
    ('Strength'),
    ('Flexibility'),
    ('HIIT'),
    ('Yoga')
    ON DUPLICATE KEY UPDATE training_type_name = VALUES(training_type_name);
