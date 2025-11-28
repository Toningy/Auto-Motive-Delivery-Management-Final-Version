-- SQL/schema.sql
-- WARNING: This script drops the database if it exists,
-- and recreates it with all tables.
-- For development/exercises. DO NOT use in production.

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

-- Orders (renamed from "order" due to reserved keyword)
CREATE TABLE orders (
                        order_id INT PRIMARY KEY AUTO_INCREMENT,
                        client_id INT NOT NULL,
                        order_date DATE,
                        total_price DECIMAL(10,2),
                        CONSTRAINT fk_orders_client
                            FOREIGN KEY (client_id) REFERENCES client (client_id)
);

-- Delivery Mission
CREATE TABLE delivery_mission (
                                  mission_id INT PRIMARY KEY AUTO_INCREMENT,
                                  order_id INT NOT NULL,
                                  delivery_man_id INT,
                                  vehicle_id INT,
                                  status VARCHAR(20),
                                  start_date DATE,
                                  end_date DATE,
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

-- Order Basket (which cars belong to which order)
CREATE TABLE order_basket (
                              order_id INT NOT NULL,
                              car_id INT NOT NULL,
                              PRIMARY KEY (order_id, car_id),
                              CONSTRAINT fk_basket_order
                                  FOREIGN KEY (order_id) REFERENCES orders (order_id),
                              CONSTRAINT fk_basket_car
                                  FOREIGN KEY (car_id) REFERENCES car (car_id)
);