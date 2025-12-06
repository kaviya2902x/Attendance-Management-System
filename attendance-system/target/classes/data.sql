-- ============================================
-- ATTENDANCE MANAGEMENT SYSTEM DATABASE
-- Database: attendance_db
-- ============================================

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS attendance_dbms;
USE attendance_dbms;

-- ============================================
-- TABLE: users
-- Stores system users (Admin & Employees)
-- ============================================
CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    employee_id VARCHAR(20) UNIQUE,
    role VARCHAR(20) NOT NULL DEFAULT 'EMPLOYEE',
    department VARCHAR(100),
    position VARCHAR(100),
    phone_number VARCHAR(20),
    date_of_birth DATE,
    date_of_joining DATE DEFAULT (CURRENT_DATE),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE: attendance
-- Stores daily attendance records
-- ============================================
CREATE TABLE IF NOT EXISTS attendance (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          user_id BIGINT NOT NULL,
                                          attendance_date DATE NOT NULL,
                                          punch_in DATETIME,
                                          punch_out DATETIME,
                                          total_hours DECIMAL(5,2),
    status VARCHAR(20) DEFAULT 'PRESENT',
    notes TEXT,
    late_minutes INT DEFAULT 0,
    overtime_hours DECIMAL(5,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_date (user_id, attendance_date),
    CONSTRAINT fk_attendance_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE: leaves
-- Stores leave applications
-- ============================================
CREATE TABLE IF NOT EXISTS leaves (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      user_id BIGINT NOT NULL,
                                      leave_type VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days INT,
    reason VARCHAR(500),
    status VARCHAR(20) DEFAULT 'PENDING',
    approved_by VARCHAR(100),
    comments TEXT,
    applied_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_on TIMESTAMP NULL,
    CONSTRAINT fk_leaves_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE: regularizations
-- Stores attendance regularization requests
-- ============================================
CREATE TABLE IF NOT EXISTS regularizations (
                                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                               user_id BIGINT NOT NULL,
                                               attendance_date DATE NOT NULL,
                                               requested_punch_in VARCHAR(10),
    requested_punch_out VARCHAR(10),
    reason VARCHAR(500),
    status VARCHAR(20) DEFAULT 'PENDING',
    approved_by VARCHAR(100),
    comments TEXT,
    requested_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_on TIMESTAMP NULL,
    CONSTRAINT fk_regularizations_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- SAMPLE DATA INSERTION
-- ============================================

-- Insert Admin User (password: admin123)
INSERT INTO users (username, password, email, first_name, last_name, employee_id, role, department, position, phone_number, date_of_joining) VALUES
    ('admin', 'admin123', 'admin@company.com', 'System', 'Administrator', 'ADM001', 'ADMIN', 'IT', 'System Administrator', '9876543210', '2024-01-01');

-- Insert Sample Employees (password: emp123 for all)
INSERT INTO users (username, password, email, first_name, last_name, employee_id, department, position, phone_number, date_of_joining) VALUES
                                                                                                                                           ('john.doe', 'emp123', 'john.doe@company.com', 'John', 'Doe', 'EMP001', 'Engineering', 'Software Engineer', '9876543211', '2024-01-15'),
                                                                                                                                           ('jane.smith', 'emp123', 'jane.smith@company.com', 'Jane', 'Smith', 'EMP002', 'Marketing', 'Marketing Manager', '9876543212', '2024-01-15'),
                                                                                                                                           ('mike.johnson', 'emp123', 'mike.johnson@company.com', 'Mike', 'Johnson', 'EMP003', 'Sales', 'Sales Executive', '9876543213', '2024-01-15'),
                                                                                                                                           ('sarah.williams', 'emp123', 'sarah.williams@company.com', 'Sarah', 'Williams', 'EMP004', 'HR', 'HR Manager', '9876543214', '2024-01-15'),
                                                                                                                                           ('robert.brown', 'emp123', 'robert.brown@company.com', 'Robert', 'Brown', 'EMP005', 'Finance', 'Accountant', '9876543215', '2024-01-15'),
                                                                                                                                           ('lisa.davis', 'emp123', 'lisa.davis@company.com', 'Lisa', 'Davis', 'EMP006', 'Operations', 'Operations Manager', '9876543216', '2024-01-15');

-- Insert Sample Attendance Records (Last 30 days)
INSERT INTO attendance (user_id, attendance_date, punch_in, punch_out, total_hours, status, late_minutes) VALUES
-- Today's attendance
(2, CURDATE(), CONCAT(CURDATE(), ' 09:15:00'), CONCAT(CURDATE(), ' 18:00:00'), 8.75, 'PRESENT', 15),
(3, CURDATE(), CONCAT(CURDATE(), ' 09:00:00'), CONCAT(CURDATE(), ' 17:30:00'), 8.50, 'PRESENT', 0),
(4, CURDATE(), NULL, NULL, NULL, 'ABSENT', 0),
(5, CURDATE(), CONCAT(CURDATE(), ' 09:30:00'), CONCAT(CURDATE(), ' 18:15:00'), 8.75, 'PRESENT', 30),

-- Yesterday
(2, DATE_SUB(CURDATE(), INTERVAL 1 DAY), CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 09:10:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 17:45:00'), 8.58, 'PRESENT', 10),
(3, DATE_SUB(CURDATE(), INTERVAL 1 DAY), CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 09:05:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 17:30:00'), 8.42, 'PRESENT', 5),
(4, DATE_SUB(CURDATE(), INTERVAL 1 DAY), CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 09:00:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 18:00:00'), 9.00, 'PRESENT', 0),

-- 2 days ago
(2, DATE_SUB(CURDATE(), INTERVAL 2 DAY), NULL, NULL, NULL, 'ABSENT', 0),
(3, DATE_SUB(CURDATE(), INTERVAL 2 DAY), CONCAT(DATE_SUB(CURDATE(), INTERVAL 2 DAY), ' 09:20:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 2 DAY), ' 17:00:00'), 7.67, 'HALF_DAY', 20),
(4, DATE_SUB(CURDATE(), INTERVAL 2 DAY), CONCAT(DATE_SUB(CURDATE(), INTERVAL 2 DAY), ' 09:00:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 2 DAY), ' 18:00:00'), 9.00, 'PRESENT', 0);

-- Insert Sample Leave Applications
INSERT INTO leaves (user_id, leave_type, start_date, end_date, total_days, reason, status, applied_on) VALUES
-- Pending Leaves
(2, 'SICK', DATE_ADD(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), 1, 'Medical appointment', 'PENDING', NOW()),
(3, 'CASUAL', DATE_ADD(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 12 DAY), 3, 'Family function', 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 'EARNED', DATE_ADD(CURDATE(), INTERVAL 15 DAY), DATE_ADD(CURDATE(), INTERVAL 20 DAY), 6, 'Vacation trip', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- Approved Leaves
(5, 'SICK', DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_SUB(CURDATE(), INTERVAL 10 DAY), 1, 'Fever', 'APPROVED', DATE_SUB(NOW(), INTERVAL 12 DAY)),
(6, 'CASUAL', DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_SUB(CURDATE(), INTERVAL 5 DAY), 1, 'Personal work', 'APPROVED', DATE_SUB(NOW(), INTERVAL 7 DAY)),

-- Rejected Leaves
(2, 'EARNED', DATE_ADD(CURDATE(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 40 DAY), 11, 'Long vacation', 'REJECTED', DATE_SUB(NOW(), INTERVAL 5 DAY));

-- Update approved leaves with approver info
UPDATE leaves SET approved_by = 'admin', processed_on = NOW() WHERE status = 'APPROVED';
UPDATE leaves SET approved_by = 'admin', processed_on = NOW(), comments = 'Too many leaves applied recently' WHERE status = 'REJECTED';

-- Insert Sample Regularization Requests
INSERT INTO regularizations (user_id, attendance_date, requested_punch_in, requested_punch_out, reason, status, requested_on) VALUES
-- Pending Requests
(2, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '09:00', '18:00', 'Forgot to punch in', 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, DATE_SUB(CURDATE(), INTERVAL 5 DAY), '09:30', '18:30', 'System error during punch', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- Approved Requests
(4, DATE_SUB(CURDATE(), INTERVAL 7 DAY), '09:15', '17:45', 'Emergency meeting', 'APPROVED', DATE_SUB(NOW(), INTERVAL 5 DAY)),

-- Rejected Requests
(5, DATE_SUB(CURDATE(), INTERVAL 10 DAY), '10:00', '19:00', 'Traffic jam', 'REJECTED', DATE_SUB(NOW(), INTERVAL 8 DAY));

-- Update processed regularizations
UPDATE regularizations SET approved_by = 'admin', processed_on = NOW(), comments = 'Approved as per HR policy' WHERE status = 'APPROVED';
UPDATE regularizations SET approved_by = 'admin', processed_on = NOW(), comments = 'No valid reason provided' WHERE status = 'REJECTED';

-- ============================================
-- INDEXES FOR PERFORMANCE OPTIMIZATION
-- ============================================

-- Users table indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_employee_id ON users(employee_id);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_department ON users(department);
CREATE INDEX idx_users_active ON users(is_active);

-- Attendance table indexes
CREATE INDEX idx_attendance_date ON attendance(attendance_date);
CREATE INDEX idx_attendance_user_id ON attendance(user_id);
CREATE INDEX idx_attendance_status ON attendance(status);
CREATE INDEX idx_attendance_user_date ON attendance(user_id, attendance_date);

-- Leaves table indexes
CREATE INDEX idx_leaves_user_id ON leaves(user_id);
CREATE INDEX idx_leaves_status ON leaves(status);
CREATE INDEX idx_leaves_start_date ON leaves(start_date);
CREATE INDEX idx_leaves_end_date ON leaves(end_date);
CREATE INDEX idx_leaves_dates ON leaves(start_date, end_date);

-- Regularizations table indexes
CREATE INDEX idx_regularizations_user_id ON regularizations(user_id);
CREATE INDEX idx_regularizations_status ON regularizations(status);
CREATE INDEX idx_regularizations_date ON regularizations(attendance_date);

-- ============================================
-- VIEWS FOR REPORTING
-- ============================================

-- View: Monthly Attendance Summary
CREATE VIEW monthly_attendance_summary AS
SELECT
    u.id as user_id,
    u.employee_id,
    CONCAT(u.first_name, ' ', u.last_name) as employee_name,
    u.department,
    u.position,
    COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) as present_days,
    COUNT(CASE WHEN a.status = 'ABSENT' THEN 1 END) as absent_days,
    COUNT(CASE WHEN a.status = 'HALF_DAY' THEN 1 END) as half_days,
    SUM(a.total_hours) as total_hours,
    SUM(a.overtime_hours) as overtime_hours,
    AVG(a.late_minutes) as avg_late_minutes
FROM users u
         LEFT JOIN attendance a ON u.id = a.user_id
    AND a.attendance_date >= DATE_FORMAT(NOW(), '%Y-%m-01')
    AND a.attendance_date <= LAST_DAY(NOW())
WHERE u.is_active = 1 AND u.role = 'EMPLOYEE'
GROUP BY u.id, u.employee_id, u.first_name, u.last_name, u.department, u.position;

-- View: Pending Approvals
CREATE VIEW pending_approvals AS
SELECT
    'LEAVE' as request_type,
    l.id as request_id,
    u.employee_id,
    CONCAT(u.first_name, ' ', u.last_name) as employee_name,
    u.department,
    l.leave_type,
    l.start_date,
    l.end_date,
    l.total_days,
    l.reason,
    l.applied_on
FROM leaves l
         JOIN users u ON l.user_id = u.id
WHERE l.status = 'PENDING'
UNION ALL
SELECT
    'REGULARIZATION' as request_type,
    r.id as request_id,
    u.employee_id,
    CONCAT(u.first_name, ' ', u.last_name) as employee_name,
    u.department,
    NULL as leave_type,
    r.attendance_date as start_date,
    r.attendance_date as end_date,
    NULL as total_days,
    r.reason,
    r.requested_on as applied_on
FROM regularizations r
         JOIN users u ON r.user_id = u.id
WHERE r.status = 'PENDING'
ORDER BY applied_on DESC;

-- View: Employee Performance
CREATE VIEW employee_performance AS
SELECT
    u.id,
    u.employee_id,
    CONCAT(u.first_name, ' ', u.last_name) as employee_name,
    u.department,
    u.position,
    u.date_of_joining,
    -- Last 30 days performance
    (SELECT COUNT(*) FROM attendance a
     WHERE a.user_id = u.id
       AND a.attendance_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
       AND a.status = 'PRESENT') as present_last_30_days,
    (SELECT COUNT(*) FROM attendance a
     WHERE a.user_id = u.id
       AND a.attendance_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
       AND a.status = 'ABSENT') as absent_last_30_days,
    (SELECT AVG(late_minutes) FROM attendance a
     WHERE a.user_id = u.id
       AND a.attendance_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)) as avg_late_minutes,
    (SELECT SUM(total_hours) FROM attendance a
     WHERE a.user_id = u.id
       AND a.attendance_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)) as total_hours_last_30_days
FROM users u
WHERE u.is_active = 1 AND u.role = 'EMPLOYEE';

-- ============================================
-- STORED PROCEDURES
-- ============================================

-- Procedure: Generate Attendance Report
DELIMITER //
CREATE PROCEDURE GenerateAttendanceReport(
    IN p_start_date DATE,
    IN p_end_date DATE,
    IN p_department VARCHAR(100)
)
BEGIN
SELECT
    u.employee_id,
    CONCAT(u.first_name, ' ', u.last_name) as employee_name,
    u.department,
    u.position,
    a.attendance_date,
    DATE_FORMAT(a.punch_in, '%h:%i %p') as punch_in_time,
    DATE_FORMAT(a.punch_out, '%h:%i %p') as punch_out_time,
    a.total_hours,
    a.status,
    a.late_minutes,
    a.overtime_hours,
    a.notes
FROM attendance a
         JOIN users u ON a.user_id = u.id
WHERE a.attendance_date BETWEEN p_start_date AND p_end_date
  AND (p_department IS NULL OR u.department = p_department)
  AND u.is_active = 1
ORDER BY a.attendance_date DESC, u.department, u.employee_id;
END //
DELIMITER ;

-- Procedure: Get Employee Statistics
DELIMITER //
CREATE PROCEDURE GetEmployeeStatistics(
    IN p_employee_id VARCHAR(20)
)
BEGIN
SELECT
    u.employee_id,
    CONCAT(u.first_name, ' ', u.last_name) as employee_name,
    u.department,
    u.position,
    u.date_of_joining,
    -- Current month stats
    (SELECT COUNT(*) FROM attendance a
     WHERE a.user_id = u.id
               AND MONTH(a.attendance_date) = MONTH(CURDATE())
         AND YEAR(a.attendance_date) = YEAR(CURDATE())
         AND a.status = 'PRESENT') as present_this_month,
        (SELECT COUNT(*) FROM attendance a
WHERE a.user_id = u.id
  AND MONTH(a.attendance_date) = MONTH(CURDATE())
  AND YEAR(a.attendance_date) = YEAR(CURDATE())
  AND a.status = 'ABSENT') as absent_this_month,
    (SELECT SUM(total_hours) FROM attendance a
WHERE a.user_id = u.id
  AND MONTH(a.attendance_date) = MONTH(CURDATE())
  AND YEAR(a.attendance_date) = YEAR(CURDATE())) as total_hours_this_month,
-- Leave balance
    (SELECT COUNT(*) FROM leaves l
WHERE l.user_id = u.id
  AND YEAR(l.start_date) = YEAR(CURDATE())
  AND l.status = 'APPROVED'
  AND l.leave_type = 'CASUAL') as casual_leaves_taken,
    (SELECT COUNT(*) FROM leaves l
WHERE l.user_id = u.id
  AND YEAR(l.start_date) = YEAR(CURDATE())
  AND l.status = 'APPROVED'
  AND l.leave_type = 'SICK') as sick_leaves_taken
FROM users u
WHERE u.employee_id = p_employee_id;
END //
DELIMITER ;

-- ============================================
-- TRIGGERS
-- ============================================

-- Trigger: Calculate total days for leaves automatically
DELIMITER //
CREATE TRIGGER calculate_leave_days
    BEFORE INSERT ON leaves
    FOR EACH ROW
BEGIN
    SET NEW.total_days = DATEDIFF(NEW.end_date, NEW.start_date) + 1;
END //
DELIMITER ;

-- Trigger: Update total hours when punch_out is set
DELIMITER //
CREATE TRIGGER calculate_attendance_hours
    BEFORE UPDATE ON attendance
    FOR EACH ROW
BEGIN
    IF NEW.punch_in IS NOT NULL AND NEW.punch_out IS NOT NULL THEN
        SET NEW.total_hours = TIMESTAMPDIFF(MINUTE, NEW.punch_in, NEW.punch_out) / 60.0;

        -- Calculate overtime (more than 8 hours)
        IF NEW.total_hours > 8 THEN
            SET NEW.overtime_hours = NEW.total_hours - 8;
    ELSE
            SET NEW.overtime_hours = 0;
END IF;

-- Calculate late minutes (punch in after 9:30 AM)
IF TIME(NEW.punch_in) > '09:30:00' THEN
            SET NEW.late_minutes = TIMESTAMPDIFF(MINUTE, '09:30:00', TIME(NEW.punch_in));
END IF;
END IF;
END //
DELIMITER ;

-- Trigger: Auto-generate employee ID
DELIMITER //
CREATE TRIGGER generate_employee_id
    BEFORE INSERT ON users
    FOR EACH ROW
BEGIN
    IF NEW.employee_id IS NULL AND NEW.role = 'EMPLOYEE' THEN
        -- Get next employee number
        DECLARE next_num INT;
    SELECT COALESCE(MAX(CAST(SUBSTRING(employee_id, 4) AS UNSIGNED)), 0) + 1
    INTO next_num
    FROM users
    WHERE employee_id LIKE 'EMP%';

    SET NEW.employee_id = CONCAT('EMP', LPAD(next_num, 3, '0'));
END IF;
END //
DELIMITER ;

-- ============================================
-- FUNCTIONS
-- ============================================

-- Function: Calculate working days between dates (excluding weekends)
DELIMITER //
CREATE FUNCTION CalculateWorkingDays(start_date DATE, end_date DATE)
    RETURNS INT
    DETERMINISTIC
BEGIN
    DECLARE total_days INT;
    DECLARE day_count INT DEFAULT 0;
    DECLARE current_date DATE;

    SET total_days = DATEDIFF(end_date, start_date) + 1;
    SET current_date = start_date;

    WHILE current_date <= end_date DO
        IF DAYOFWEEK(current_date) NOT IN (1, 7) THEN -- Exclude Sunday(1) and Saturday(7)
            SET day_count = day_count + 1;
END IF;
        SET current_date = DATE_ADD(current_date, INTERVAL 1 DAY);
END WHILE;

RETURN day_count;
END //
DELIMITER ;

-- Function: Get employee tenure in years
DELIMITER //
CREATE FUNCTION GetEmployeeTenure(employee_id_param VARCHAR(20))
    RETURNS DECIMAL(5,2)
    READS SQL DATA
BEGIN
    DECLARE tenure_years DECIMAL(5,2);

SELECT TIMESTAMPDIFF(MONTH, date_of_joining, CURDATE()) / 12.0
INTO tenure_years
FROM users
WHERE employee_id = employee_id_param;

RETURN COALESCE(tenure_years, 0);
END //
DELIMITER ;

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- Show all created tables
SHOW TABLES;

-- Count records in each table
SELECT
    'users' as table_name,
    COUNT(*) as record_count
FROM users
UNION ALL
SELECT
    'attendance',
    COUNT(*)
FROM attendance
UNION ALL
SELECT
    'leaves',
    COUNT(*)
FROM leaves
UNION ALL
SELECT
    'regularizations',
    COUNT(*)
FROM regularizations;

-- Show sample data
SELECT
    'Active Users' as category,
    CONCAT(COUNT(*), ' users') as value
FROM users
WHERE is_active = 1
UNION ALL
SELECT
    'Employees',
    CONCAT(COUNT(*), ' employees')
FROM users
WHERE role = 'EMPLOYEE' AND is_active = 1
UNION ALL
SELECT
    'Today''s Present',
    CONCAT(COUNT(*), ' employees')
FROM attendance
WHERE attendance_date = CURDATE() AND status = 'PRESENT'
UNION ALL
SELECT
    'Pending Leaves',
    CONCAT(COUNT(*), ' requests')
FROM leaves
WHERE status = 'PENDING'
UNION ALL
SELECT
    'Pending Regularizations',
    CONCAT(COUNT(*), ' requests')
FROM regularizations
WHERE status = 'PENDING';

-- Test the views
SELECT * FROM monthly_attendance_summary LIMIT 5;
SELECT * FROM pending_approvals LIMIT 5;
SELECT * FROM employee_performance LIMIT 5;

-- Test stored procedure
CALL GenerateAttendanceReport(DATE_SUB(CURDATE(), INTERVAL 7 DAY), CURDATE(), NULL);

-- ============================================
-- DATABASE MAINTENANCE QUERIES
-- ============================================

-- Show table sizes
SELECT
    TABLE_NAME as `Table`,
    ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 2) as `Size (MB)`,
    TABLE_ROWS as `Rows`
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'attendance_db'
ORDER BY DATA_LENGTH + INDEX_LENGTH DESC;

-- Show index usage
SELECT
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'attendance_db'
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- ============================================
-- CLEANUP QUERIES (For testing/reset)
-- ============================================

/*
-- WARNING: These will delete all data!
-- Reset database (use with caution)

-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Truncate all tables
TRUNCATE TABLE regularizations;
TRUNCATE TABLE leaves;
TRUNCATE TABLE attendance;
DELETE FROM users WHERE username != 'admin';

-- Enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;
*/

-- ============================================
-- FINAL SUCCESS MESSAGE
-- ============================================
SELECT '✅ DATABASE SETUP COMPLETED SUCCESSFULLY!' as message;

-- Show database information
SELECT
    DATABASE() as database_name,
    VERSION() as mysql_version,
    @@hostname as server_host,
    CURRENT_TIMESTAMP as setup_time;