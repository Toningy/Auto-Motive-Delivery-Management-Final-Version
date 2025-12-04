-- COMPLETE SCHEMA FOR SERVLET/JDBC VERSION
DROP DATABASE IF EXISTS automotive_delivery_db;
CREATE DATABASE automotive_delivery_db;
USE automotive_delivery_db;

-- =========================
-- Tables
-- =========================

-- Base Person table
CREATE TABLE person (
    person_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(25),
    birthdate DATE,
    email VARCHAR(50)
);

-- Client (Customer) – 1:1 with Person
CREATE TABLE client (
    client_id INT PRIMARY KEY,
    shipping_address VARCHAR(250),
    billing_address VARCHAR(250),
    CONSTRAINT fk_client_person
        FOREIGN KEY (client_id) REFERENCES person (person_id)
);

-- Staff (Employee) – 1:1 with Person
CREATE TABLE staff (
    staff_id INT PRIMARY KEY,
    salary DECIMAL(10,2),
    working_email VARCHAR(50),
    taxation_number VARCHAR(20),
    CONSTRAINT fk_staff_person
        FOREIGN KEY (staff_id) REFERENCES person (person_id)
);

-- Warehouse
CREATE TABLE warehouse (
    warehouse_id INT PRIMARY KEY AUTO_INCREMENT,
    capacity INT
);

-- Factory
CREATE TABLE factory (
    factory_id INT PRIMARY KEY AUTO_INCREMENT,
    location VARCHAR(255)
);

-- Delivery Vehicle
CREATE TABLE delivery_vehicle (
    vehicle_id INT PRIMARY KEY AUTO_INCREMENT,
    plate_number VARCHAR(50)
);

-- Manager (Specialization of Staff)
CREATE TABLE manager (
    staff_id INT PRIMARY KEY,
    factory_id INT,
    warehouse_id INT,
    CONSTRAINT fk_manager_staff
        FOREIGN KEY (staff_id) REFERENCES staff (staff_id),
    CONSTRAINT fk_manager_factory
        FOREIGN KEY (factory_id) REFERENCES factory (factory_id),
    CONSTRAINT fk_manager_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouse (warehouse_id)
);

-- Delivery Man (Specialization of Staff)
CREATE TABLE delivery_man (
    staff_id INT PRIMARY KEY,
    license_number VARCHAR(50),
    vehicle_id INT,
    CONSTRAINT fk_deliveryman_staff
        FOREIGN KEY (staff_id) REFERENCES staff (staff_id),
    CONSTRAINT fk_deliveryman_vehicle
        FOREIGN KEY (vehicle_id) REFERENCES delivery_vehicle (vehicle_id)
);

-- Car
CREATE TABLE car (
    car_id INT PRIMARY KEY AUTO_INCREMENT,
    model_name VARCHAR(50),
    model_year INT,
    weight DECIMAL(10,2),
    price DECIMAL(10,2),
    warehouse_id INT,
    factory_id INT,
    CONSTRAINT fk_car_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouse (warehouse_id),
    CONSTRAINT fk_car_factory
        FOREIGN KEY (factory_id) REFERENCES factory (factory_id)
);

-- Orders
CREATE TABLE orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    client_id INT NOT NULL,
    order_date DATE,
    total_price DECIMAL(10,2),
    status VARCHAR(20) DEFAULT 'PENDING',
    CONSTRAINT fk_orders_client
        FOREIGN KEY (client_id) REFERENCES client (client_id)
);

-- Delivery Mission
CREATE TABLE delivery_mission (
    mission_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT,
    delivery_man_id INT,
    vehicle_id INT,
    status VARCHAR(20) DEFAULT 'PENDING',
    start_date DATE,
    end_date DATE,
    delivery_address VARCHAR(255),
    customer_name VARCHAR(100),
    customer_phone VARCHAR(25),
    CONSTRAINT fk_mission_order
        FOREIGN KEY (order_id) REFERENCES orders (order_id),
    CONSTRAINT fk_mission_deliveryman
        FOREIGN KEY (delivery_man_id) REFERENCES delivery_man (staff_id),
    CONSTRAINT fk_mission_vehicle
        FOREIGN KEY (vehicle_id) REFERENCES delivery_vehicle (vehicle_id)
);

-- Invoice
CREATE TABLE invoice (
    invoice_id INT PRIMARY KEY AUTO_INCREMENT,
    client_id INT NOT NULL,
    order_id INT NOT NULL,
    issue_date DATE,
    due_date DATE,
    amount DECIMAL(10,2),
    payment_status VARCHAR(50),
    CONSTRAINT fk_invoice_client
        FOREIGN KEY (client_id) REFERENCES client (client_id),
    CONSTRAINT fk_invoice_order
        FOREIGN KEY (order_id) REFERENCES orders (order_id)
);

-- Payment
CREATE TABLE payment (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    payment_date DATE,
    amount DECIMAL(10,2),
    invoice_id INT NOT NULL,
    CONSTRAINT fk_payment_invoice
        FOREIGN KEY (invoice_id) REFERENCES invoice (invoice_id)
);

-- Order Basket
CREATE TABLE order_basket (
    order_id INT NOT NULL,
    car_id INT NOT NULL,
    PRIMARY KEY (order_id, car_id),
    CONSTRAINT fk_basket_order
        FOREIGN KEY (order_id) REFERENCES orders (order_id),
    CONSTRAINT fk_basket_car
        FOREIGN KEY (car_id) REFERENCES car (car_id)
);

-- Users table for authentication
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    role VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert demo users
INSERT INTO users (email, password, name, role) VALUES 
('client@example.com', 'password123', 'John Client', 'CLIENT'),
('manager@example.com', 'password123', 'Alex Manager', 'MANAGER'),
('delivery@example.com', 'password123', 'Olivia Driver', 'DELIVERY_MAN');

-- Insert test data
INSERT INTO person (name, phone_number, birthdate, email) VALUES
    ('John Doe', '+852-9000-0001', '1980-01-01', 'john.doe@example.com'),
    ('Jane Smith', '+852-9000-0002', '1990-02-02', 'jane.smith@example.com'),
    ('Michael Chen', '+852-9000-0003', '1985-05-15', 'michael.chen@example.com'),
    ('Sarah Wong', '+852-9000-0004', '1992-08-20', 'sarah.wong@example.com'),
    ('Alex Manager', '+852-8000-0001', '1985-03-03', 'alex.m@company.com'),
    ('Rachel Green', '+852-8000-0002', '1988-04-04', 'rachel.g@company.com'),
    ('Olivia Driver', '+852-8000-0003', '1992-05-05', 'olivia.d@company.com'),
    ('Jason Lee', '+852-8000-0004', '1990-07-10', 'jason.l@company.com');

INSERT INTO client (client_id, shipping_address, billing_address) VALUES
    (1, '101 Nathan Road, Kowloon', '101 Nathan Road, Kowloon'),
    (2, '202 Queen''s Road Central, HK Island', '202 Queen''s Road Central, HK Island'),
    (3, '303 Hennessy Road, Wan Chai', '303 Hennessy Road, Wan Chai'),
    (4, '404 Canton Road, Tsim Sha Tsui', '404 Canton Road, Tsim Sha Tsui');

INSERT INTO warehouse (capacity) VALUES (500), (750);
INSERT INTO factory (location) VALUES 
    ('Shenzhen Manufacturing Complex, China'),
    ('Guangzhou Auto Plant, China');

INSERT INTO delivery_vehicle (plate_number) VALUES ('HK-DV-001'), ('HK-DV-002');

INSERT INTO staff (staff_id, salary, working_email, taxation_number) VALUES
    (5, 65000.00, 'alex.m@automotive.com', 'TAX-MGR-001'),
    (6, 64000.00, 'rachel.g@automotive.com', 'TAX-MGR-002'),
    (7, 38000.00, 'olivia.d@automotive.com', 'TAX-DRV-001'),
    (8, 39000.00, 'jason.l@automotive.com', 'TAX-DRV-002');

INSERT INTO manager (staff_id, factory_id, warehouse_id) VALUES
    (5, 1, 1),
    (6, 2, 2);

INSERT INTO delivery_man (staff_id, license_number, vehicle_id) VALUES
    (7, 'DL-HK-2020001', 1),
    (8, 'DL-HK-2020002', 2);

INSERT INTO car (model_name, model_year, weight, price, warehouse_id, factory_id) VALUES
    ('Toyota Corolla', 2023, 1320.00, 190000.00, 1, 1),
    ('Toyota Yaris Cross', 2023, 1270.00, 210000.00, 1, 1),
    ('Toyota Prius', 2024, 1400.00, 260000.00, 1, 1),
    ('Toyota Camry', 2023, 1510.00, 270000.00, 1, 1),
    ('Toyota C-HR', 2023, 1440.00, 230000.00, 1, 1),
    ('Toyota RAV4', 2024, 1680.00, 290000.00, 2, 2),
    ('Toyota Highlander', 2023, 1880.00, 380000.00, 2, 2),
    ('Toyota Hilux', 2023, 2040.00, 320000.00, 2, 2),
    ('Toyota Land Cruiser 300', 2024, 2480.00, 780000.00, 2, 2),
    ('Toyota GR Supra', 2023, 1520.00, 520000.00, 2, 2);