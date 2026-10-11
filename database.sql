CREATE DATABASE IF NOT EXISTS jewelry_db CHARACTER SET utf8mb4;
USE jewelry_db;

CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  category ENUM('earring','necklace','ring','bracelet') NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  image_url VARCHAR(255) NOT NULL,
  tryon_image VARCHAR(255) NOT NULL
);

INSERT INTO products (name, category, price, image_url, tryon_image) VALUES
('Gold Hoop Earrings','earring',4999,'images/earring1.png','images/earring1.png'),
('Silver Choker Necklace','necklace',12999,'images/necklace1.png','images/necklace1.png'),
('Infinity Diamond Ring','ring',24999,'images/ring1.png','images/ring1.png');