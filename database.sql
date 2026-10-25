CREATE DATABASE pharmacy_db;
USE pharmacy_db;

CREATE TABLE medicines (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    quantity INT NOT NULL DEFAULT 0,
    price DECIMAL(10,2) NOT NULL,
    expiry_date DATE
);

CREATE TABLE sales (
    id INT PRIMARY KEY AUTO_INCREMENT,
    medicine_id INT NOT NULL,
    quantity INT NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (medicine_id) REFERENCES medicines(id)
);

INSERT INTO medicines (name, category, quantity, price, expiry_date)
VALUES
('Paracetamol', 'Tablet', 100, 2.50, '2027-12-31'),
('Cetirizine', 'Tablet', 50, 3.00, '2027-10-31'),
('Vitamin C', 'Supplement', 80, 5.00, '2027-11-30');
