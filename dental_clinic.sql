-- =============================================
-- DENTAL CLINIC MANAGEMENT SYSTEM DATABASE
-- =============================================

-- Create Database
CREATE DATABASE IF NOT EXISTS dental_clinic;
USE dental_clinic;

-- =============================================
-- TABLE: users (Authentication & Roles)
-- =============================================
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('admin', 'staff') NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =============================================
-- TABLE: patients
-- =============================================
CREATE TABLE IF NOT EXISTS patients (
    patient_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    date_of_birth DATE,
    gender ENUM('Male', 'Female', 'Other'),
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    address TEXT,
    medical_history TEXT,
    emergency_contact VARCHAR(20),
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- =============================================
-- TABLE: appointments
-- =============================================
CREATE TABLE IF NOT EXISTS appointments (
    appointment_id INT PRIMARY KEY AUTO_INCREMENT,
    patient_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    duration_minutes INT DEFAULT 30,
    reason TEXT,
    status ENUM('Scheduled', 'Completed', 'Cancelled', 'No-Show') DEFAULT 'Scheduled',
    notes TEXT,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- =============================================
-- TABLE: treatments
-- =============================================
CREATE TABLE IF NOT EXISTS treatments (
    treatment_id INT PRIMARY KEY AUTO_INCREMENT,
    treatment_name VARCHAR(100) NOT NULL,
    description TEXT,
    cost DECIMAL(10, 2) NOT NULL,
    duration_minutes INT DEFAULT 30,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =============================================
-- TABLE: patient_treatments (Junction table)
-- =============================================
CREATE TABLE IF NOT EXISTS patient_treatments (
    patient_treatment_id INT PRIMARY KEY AUTO_INCREMENT,
    patient_id INT NOT NULL,
    treatment_id INT NOT NULL,
    appointment_id INT,
    tooth_number VARCHAR(5),
    notes TEXT,
    status ENUM('Planned', 'In Progress', 'Completed', 'Cancelled') DEFAULT 'Planned',
    performed_by INT,
    treatment_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (treatment_id) REFERENCES treatments(treatment_id) ON DELETE CASCADE,
    FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id) ON DELETE SET NULL,
    FOREIGN KEY (performed_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- =============================================
-- TABLE: billing
-- =============================================
CREATE TABLE IF NOT EXISTS billing (
    bill_id INT PRIMARY KEY AUTO_INCREMENT,
    patient_id INT NOT NULL,
    appointment_id INT,
    patient_treatment_id INT,
    total_amount DECIMAL(10, 2) NOT NULL,
    paid_amount DECIMAL(10, 2) DEFAULT 0.00,
    discount DECIMAL(10, 2) DEFAULT 0.00,
    tax DECIMAL(10, 2) DEFAULT 0.00,
    payment_status ENUM('Pending', 'Partial', 'Paid', 'Refunded') DEFAULT 'Pending',
    payment_method ENUM('Cash', 'Credit Card', 'Debit Card', 'Insurance', 'Other'),
    payment_date DATE,
    invoice_number VARCHAR(50) UNIQUE,
    notes TEXT,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id) ON DELETE SET NULL,
    FOREIGN KEY (patient_treatment_id) REFERENCES patient_treatments(patient_treatment_id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- =============================================
-- INDEXES FOR BETTER PERFORMANCE
-- =============================================

CREATE INDEX idx_patient_name ON patients(first_name, last_name);
CREATE INDEX idx_appointment_date ON appointments(appointment_date);
CREATE INDEX idx_payment_status ON billing(payment_status);
CREATE INDEX idx_username ON users(username);
CREATE INDEX idx_appointment_patient ON appointments(patient_id);
CREATE INDEX idx_treatment_date ON patient_treatments(treatment_date);

-- =============================================
-- VIEW: Appointment Summary
-- =============================================

CREATE OR REPLACE VIEW appointment_summary AS
SELECT 
    a.appointment_id,
    CONCAT(p.first_name, ' ', p.last_name) AS patient_name,
    a.appointment_date,
    a.appointment_time,
    a.status,
    a.reason
FROM appointments a
JOIN patients p ON a.patient_id = p.patient_id;

-- =============================================
-- VIEW: Billing Summary
-- =============================================

CREATE OR REPLACE VIEW billing_summary AS
SELECT 
    b.bill_id,
    b.invoice_number,
    CONCAT(p.first_name, ' ', p.last_name) AS patient_name,
    b.total_amount,
    b.paid_amount,
    (b.total_amount - b.paid_amount) AS balance_due,
    b.payment_status,
    b.created_at
FROM billing b
JOIN patients p ON b.patient_id = p.patient_id;

-- =============================================
-- STORED PROCEDURE: Get Patient Appointment History
-- =============================================

DELIMITER //
CREATE PROCEDURE GetPatientHistory(IN patientId INT)
BEGIN
    SELECT 
        a.appointment_date,
        a.appointment_time,
        a.status,
        t.treatment_name,
        pt.notes,
        b.payment_status,
        b.total_amount,
        b.paid_amount
    FROM appointments a
    LEFT JOIN patient_treatments pt ON a.appointment_id = pt.appointment_id
    LEFT JOIN treatments t ON pt.treatment_id = t.treatment_id
    LEFT JOIN billing b ON a.appointment_id = b.appointment_id
    WHERE a.patient_id = patientId
    ORDER BY a.appointment_date DESC, a.appointment_time DESC;
END //
DELIMITER ;

-- =============================================
-- TRIGGER: Generate Invoice Number Automatically
-- =============================================

DELIMITER //
CREATE TRIGGER generate_invoice_number
BEFORE INSERT ON billing
FOR EACH ROW
BEGIN
    DECLARE next_number INT;
    DECLARE new_invoice VARCHAR(50);
    
    SELECT IFNULL(MAX(CAST(SUBSTRING(invoice_number, 4) AS UNSIGNED)), 0) + 1
    INTO next_number
    FROM billing
    WHERE invoice_number LIKE 'INV%';
    
    SET new_invoice = CONCAT('INV', LPAD(next_number, 6, '0'));
    SET NEW.invoice_number = new_invoice;
END //
DELIMITER ;

-- =============================================
-- QUERY EXAMPLES (Commented)
-- =============================================

-- Login Query:
-- SELECT * FROM users WHERE username = 'akram' AND password = 'akram';

-- Get Today's Appointments:
-- SELECT * FROM appointments WHERE appointment_date = CURDATE();

-- Get Pending Payments:
-- SELECT * FROM billing WHERE payment_status = 'Pending';

-- Get Patient by Name:
-- SELECT * FROM patients WHERE first_name LIKE '%John%' OR last_name LIKE '%John%';

-- =============================================
-- END OF DATABASE SCRIPT
-- =============================================










-- INSERT Data Part:


-- =============================================
-- INSERT DEFAULT USERS
-- =============================================

-- Admin User: akram / akram
INSERT INTO users (username, password, role, full_name, email, phone) VALUES
('akram', 'akram', 'admin', 'Akram_Administrator', 'akramkarimi798@gmail.com', '0782774803');

-- Staff User: staff / 123
INSERT INTO users (username, password, role, full_name, email, phone) VALUES
('staff', '123', 'staff', 'Staff_Member', 'staff@gmail.com', '0782774803');

-- =============================================
-- INSERT SAMPLE TREATMENTS
-- =============================================

INSERT INTO treatments (treatment_name, description, cost, duration_minutes) VALUES
('Teeth Cleaning', 'Professional dental cleaning and polishing', 150.00, 30),
('Filling', 'Tooth filling for cavities', 200.00, 45),
('Root Canal', 'Endodontic treatment', 800.00, 90),
('Tooth Extraction', 'Surgical tooth removal', 300.00, 60),
('Dental Crown', 'Tooth crown placement', 1000.00, 120),
('Teeth Whitening', 'Professional teeth whitening', 500.00, 60),
('X-Ray', 'Dental X-ray imaging', 100.00, 15),
('Orthodontic Consultation', 'Braces and alignment consultation', 250.00, 45),
('Implant', 'Dental implant surgery', 2500.00, 120),
('Dental Checkup', 'Regular dental examination', 100.00, 30);

