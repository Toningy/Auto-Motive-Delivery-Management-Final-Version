-- SQL/simple_testdata.sql
-- Simplified test data for automotive_delivery_db
-- Run this AFTER schema.sql

USE automotive_delivery_db;

-- =========================
-- PERSON (Base table)
-- =========================
INSERT INTO person (name, phone_number, birthdate, email) VALUES
                                                              ('John Doe', '+852-9000-0001', '1980-01-01', 'john.doe@example.com'),           -- ID 1 (Client)
                                                              ('Jane Smith', '+852-9000-0002', '1990-02-02', 'jane.smith@example.com'),       -- ID 2 (Client)
                                                              ('Michael Chen', '+852-9000-0003', '1985-05-15', 'michael.chen@example.com'),   -- ID 3 (Client)
                                                              ('Sarah Wong', '+852-9000-0004', '1992-08-20', 'sarah.wong@example.com'),       -- ID 4 (Client)
                                                              ('Alex Manager', '+852-8000-0001', '1985-03-03', 'alex.m@company.com'),         -- ID 5 (Staff/Manager)
                                                              ('Rachel Green', '+852-8000-0002', '1988-04-04', 'rachel.g@company.com'),       -- ID 6 (Staff/Manager)
                                                              ('Olivia Driver', '+852-8000-0003', '1992-05-05', 'olivia.d@company.com'),      -- ID 7 (Staff/Delivery Man)
                                                              ('Jason Lee', '+852-8000-0004', '1990-07-10', 'jason.l@company.com');           -- ID 8 (Staff/Delivery Man)

-- =========================
-- CLIENT
-- =========================
INSERT INTO client (client_id, shipping_address, billing_address) VALUES
                                                                      (1, '101 Nathan Road, Kowloon', '101 Nathan Road, Kowloon'),
                                                                      (2, '202 Queen''s Road Central, HK Island', '202 Queen''s Road Central, HK Island'),
                                                                      (3, '303 Hennessy Road, Wan Chai', '303 Hennessy Road, Wan Chai'),
                                                                      (4, '404 Canton Road, Tsim Sha Tsui', '404 Canton Road, Tsim Sha Tsui');

-- =========================
-- STAFF
-- =========================
INSERT INTO staff (staff_id, salary, working_email, taxation_number) VALUES
                                                                         (5, 65000.00, 'alex.m@automotive.com', 'TAX-MGR-001'),
                                                                         (6, 64000.00, 'rachel.g@automotive.com', 'TAX-MGR-002'),
                                                                         (7, 38000.00, 'olivia.d@automotive.com', 'TAX-DRV-001'),
                                                                         (8, 39000.00, 'jason.l@automotive.com', 'TAX-DRV-002');

-- =========================
-- WAREHOUSE
-- =========================
INSERT INTO warehouse (capacity) VALUES
                                     (500),  -- ID 1
                                     (750);  -- ID 2

-- =========================
-- FACTORY
-- =========================
INSERT INTO factory (location) VALUES
                                   ('Shenzhen Manufacturing Complex, China'),  -- ID 1
                                   ('Guangzhou Auto Plant, China');            -- ID 2

-- =========================
-- DELIVERY VEHICLE
-- =========================
INSERT INTO delivery_vehicle (plate_number) VALUES
                                                ('HK-DV-001'),  -- ID 1
                                                ('HK-DV-002');  -- ID 2

-- =========================
-- MANAGER
-- =========================
INSERT INTO manager (staff_id, factory_id, warehouse_id) VALUES
                                                             (5, 1, 1),  -- Alex manages Factory 1 & Warehouse 1
                                                             (6, 2, 2);  -- Rachel manages Factory 2 & Warehouse 2

-- =========================
-- DELIVERY MAN
-- =========================
INSERT INTO delivery_man (staff_id, license_number, vehicle_id) VALUES
                                                                    (7, 'DL-HK-2020001', 1),  -- Olivia is a driver with vehicle 1
                                                                    (8, 'DL-HK-2020002', 2);  -- Jason is a driver with vehicle 2

-- =========================
-- CAR (10 Toyota cars with realistic specs)
-- =========================
INSERT INTO car (model_name, model_year, weight, price, warehouse_id, factory_id) VALUES
                                                                                      -- Warehouse 1 / Factory 1: Kompakte & Limousinen
                                                                                      ('Toyota Corolla', 2023, 1320.00, 190000.00, 1, 1),                 -- ID 1: Kompakte Limousine
                                                                                      ('Toyota Yaris Cross', 2023, 1270.00, 210000.00, 1, 1),             -- ID 2: Kleiner Crossover
                                                                                      ('Toyota Prius', 2024, 1400.00, 260000.00, 1, 1),                   -- ID 3: Hybrid-Limousine
                                                                                      ('Toyota Camry', 2023, 1510.00, 270000.00, 1, 1),                   -- ID 4: Mittelklasse-Limousine
                                                                                      ('Toyota C-HR', 2023, 1440.00, 230000.00, 1, 1),                    -- ID 5: Kompakt-SUV

                                                                                      -- Warehouse 2 / Factory 2: SUV, Pickup & Performance
                                                                                      ('Toyota RAV4', 2024, 1680.00, 290000.00, 2, 2),                    -- ID 6: Kompakt-SUV
                                                                                      ('Toyota Highlander', 2023, 1880.00, 380000.00, 2, 2),              -- ID 7: 3-Reihen SUV
                                                                                      ('Toyota Hilux', 2023, 2040.00, 320000.00, 2, 2),                   -- ID 8: Pickup Truck
                                                                                      ('Toyota Land Cruiser 300', 2024, 2480.00, 780000.00, 2, 2),        -- ID 9: Full-Size Offroader
                                                                                      ('Toyota GR Supra', 2023, 1520.00, 520000.00, 2, 2);                -- ID 10: Sportwagen

-- =========================
-- ORDERS (mit total_price)
-- =========================
INSERT INTO orders (client_id, order_date, total_price) VALUES
                                                            (1, '2025-01-05', 450000.00),   -- ID 1: Client John (Corolla + Prius)
                                                            (2, '2025-01-06', 290000.00),   -- ID 2: Client Jane (RAV4)
                                                            (3, '2025-01-07', 780000.00),   -- ID 3: Client Michael (Land Cruiser 300)
                                                            (4, '2025-01-08', 520000.00),   -- ID 4: Client Sarah (GR Supra)
                                                            (1, '2025-01-09', 500000.00);   -- ID 5: Client John (Camry + C-HR)

-- =========================
-- DELIVERY MISSION (mit order_id, vehicle_id, start_date, end_date)
-- =========================
INSERT INTO delivery_mission (order_id, delivery_man_id, vehicle_id, status, start_date, end_date) VALUES
                                                                                                       (1, 7, 1, 'completed', '2025-01-10', '2025-01-11'),        -- Order 1, Driver Olivia, Vehicle 1
                                                                                                       (2, 8, 2, 'in_progress', '2025-01-11', NULL),              -- Order 2, Driver Jason, Vehicle 2
                                                                                                       (3, 7, 1, 'pending', '2025-01-12', NULL);                  -- Order 3, Driver Olivia, Vehicle 1

-- =========================
-- ORDER BASKET (cars in orders)
-- =========================
INSERT INTO order_basket (order_id, car_id) VALUES
                                                (1, 1),  -- Order 1: Toyota Corolla
                                                (1, 3),  -- Order 1: Toyota Prius
                                                (2, 6),  -- Order 2: Toyota RAV4
                                                (3, 9),  -- Order 3: Toyota Land Cruiser 300
                                                (4, 10), -- Order 4: Toyota GR Supra
                                                (5, 4),  -- Order 5: Toyota Camry
                                                (5, 5);  -- Order 5: Toyota C-HR

-- =========================
-- INVOICE
-- =========================
INSERT INTO invoice (client_id, order_id, issue_date, due_date, amount, payment_status) VALUES
                                                                                            (1, 1, '2025-01-06', '2025-02-06', 450000.00, 'paid'),      -- Corolla + Prius
                                                                                            (2, 2, '2025-01-07', '2025-02-07', 290000.00, 'paid'),      -- RAV4
                                                                                            (3, 3, '2025-01-08', '2025-02-08', 780000.00, 'partial'),   -- Land Cruiser 300
                                                                                            (4, 4, '2025-01-09', '2025-02-09', 520000.00, 'pending'),   -- GR Supra
                                                                                            (1, 5, '2025-01-10', '2025-02-10', 500000.00, 'pending');   -- Camry + C-HR

-- =========================
-- PAYMENT
-- =========================
INSERT INTO payment (payment_date, amount, invoice_id) VALUES
                                                           ('2025-01-08', 450000.00, 1),   -- Full payment for invoice 1
                                                           ('2025-01-09', 290000.00, 2),   -- Full payment for invoice 2
                                                           ('2025-01-10', 400000.00, 3);   -- Partial payment for invoice 3