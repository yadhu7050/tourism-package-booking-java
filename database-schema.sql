-- Tourism Management System Database Schema
-- MySQL Database

CREATE DATABASE IF NOT EXISTS tourism_db;
USE tourism_db;

-- Users table for user authentication
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL, -- MD5 hashed
    phone VARCHAR(20),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE'
);

-- Destinations table for storing destination information
CREATE TABLE destinations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    rating DECIMAL(2,1) DEFAULT 0.0,
    reviews INT DEFAULT 0,
    price DECIMAL(10,2) NOT NULL,
    duration VARCHAR(20),
    category ENUM('Beach', 'Adventure', 'City', 'Cultural', 'Nature') NOT NULL,
    featured BOOLEAN DEFAULT FALSE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE'
);

-- Tour packages table
CREATE TABLE packages (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    destination VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    original_price DECIMAL(10,2),
    duration VARCHAR(30) NOT NULL,
    max_guests INT DEFAULT 1,
    category ENUM('Luxury', 'Adventure', 'Cultural', 'Budget', 'Family') NOT NULL,
    image_url VARCHAR(500),
    features TEXT, -- JSON string of features
    includes TEXT, -- JSON string of included items
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE'
);

-- Bookings table for storing booking information
CREATE TABLE bookings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    checkin_date DATE NOT NULL,
    checkout_date DATE NOT NULL,
    guests INT NOT NULL,
    package_id INT NOT NULL,
    special_requests TEXT,
    total_amount DECIMAL(10,2),
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED') DEFAULT 'PENDING',
    payment_status ENUM('PENDING', 'PAID', 'REFUNDED') DEFAULT 'PENDING',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (package_id) REFERENCES packages(id) ON DELETE CASCADE
);

-- Reviews table for customer reviews
CREATE TABLE reviews (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    package_id INT,
    destination_id INT,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    review_text TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (package_id) REFERENCES packages(id) ON DELETE CASCADE,
    FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
);

-- Contact inquiries table
CREATE TABLE contact_inquiries (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    subject VARCHAR(200),
    message TEXT NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('NEW', 'IN_PROGRESS', 'RESOLVED') DEFAULT 'NEW'
);

-- Newsletter subscribers table
CREATE TABLE newsletter_subscribers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) UNIQUE NOT NULL,
    subscribed_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('ACTIVE', 'UNSUBSCRIBED') DEFAULT 'ACTIVE'
);

-- Insert sample data for destinations
INSERT INTO destinations (name, location, image_url, rating, reviews, price, duration, category, featured) VALUES
('Tropical Paradise Islands', 'Maldives', 'https://images.unsplash.com/photo-1736524972348-85c310d7815b', 4.9, 124, 108000.00, '7 Days', 'Beach', TRUE),
('Mountain Adventure Trek', 'Nepal Himalayas', 'https://images.unsplash.com/photo-1595368062405-e4d7840cba14', 4.8, 89, 74999.00, '10 Days', 'Adventure', TRUE),
('Urban Exploration', 'Tokyo, Japan', 'https://images.unsplash.com/photo-1742516014153-6cae2ae4a6a2', 4.7, 156, 133500.00, '5 Days', 'City', TRUE),
('Cultural Heritage Tour', 'Rajasthan, India', 'https://images.unsplash.com/photo-1655910843284-68e2a2f37f47', 4.6, 93, 62499.00, '8 Days', 'Cultural', TRUE);

-- Insert sample data for packages
INSERT INTO packages (title, description, destination, price, original_price, duration, max_guests, category, image_url, features, includes) VALUES
('Luxury Beach Resort Package', 
 'Experience ultimate relaxation at our premium beachfront resort with world-class amenities.',
 'Maldives', 
 208250.00, 249900.00, '7 Days / 6 Nights', 4, 'Luxury',
 'https://images.unsplash.com/photo-1731080647266-85cf1bc27162',
 '["5-Star Resort", "All Meals Included", "Spa Access", "Airport Transfer"]',
 '["Free WiFi", "Airport Transfer", "Photo Session", "Travel Insurance"]'),

('Adventure Mountain Expedition',
 'Thrilling mountain adventure with guided trekking, camping, and breathtaking views.',
 'Nepal Himalayas',
 108249.00, 133417.00, '10 Days / 9 Nights', 8, 'Adventure',
 'https://images.unsplash.com/photo-1595368062405-e4d7840cba14',
 '["Expert Guide", "Camping Equipment", "All Meals", "Safety Gear"]',
 '["Safety Equipment", "Transportation", "Photo Guide", "Group Activities"]'),

('Cultural Heritage Discovery',
 'Immerse yourself in rich cultural heritage with guided tours of historical landmarks.',
 'Rajasthan, India',
 74999.00, 99999.00, '8 Days / 7 Nights', 12, 'Cultural',
 'https://images.unsplash.com/photo-1655910843284-68e2a2f37f47',
 '["Historical Tours", "Local Cuisine", "Cultural Shows", "Museum Visits"]',
 '["Expert Guide", "Photography", "Transport", "Communication"]');

-- Insert sample users (password is 'password123' in MD5)
INSERT INTO users (first_name, last_name, email, password, phone) VALUES
('John', 'Doe', 'john.doe@email.com', MD5('password123'), '+91 98765 43210'),
('Jane', 'Smith', 'jane.smith@email.com', MD5('password123'), '+91 98765 43211'),
('Raj', 'Patel', 'raj.patel@email.com', MD5('password123'), '+91 98765 43212');

-- Insert sample bookings
INSERT INTO bookings (user_id, first_name, last_name, email, phone, checkin_date, checkout_date, guests, package_id, total_amount, status) VALUES
(1, 'John', 'Doe', 'john.doe@email.com', '+91 98765 43210', '2025-12-15', '2025-12-22', 2, 1, 416500.00, 'CONFIRMED'),
(2, 'Jane', 'Smith', 'jane.smith@email.com', '+91 98765 43211', '2025-11-10', '2025-11-20', 4, 2, 432996.00, 'PENDING'),
(3, 'Raj', 'Patel', 'raj.patel@email.com', '+91 98765 43212', '2025-10-25', '2025-11-02', 6, 3, 449994.00, 'CONFIRMED');

-- Insert sample reviews
INSERT INTO reviews (user_id, package_id, destination_id, rating, review_text) VALUES
(1, 1, 1, 5, 'Amazing experience! The resort was fantastic and the staff was very helpful.'),
(2, 2, 2, 4, 'Great adventure trip. The trekking was challenging but worth it.'),
(3, 3, 4, 5, 'Wonderful cultural experience. Learned so much about the local history and traditions.');

-- Create indexes for better performance
CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_package_id ON bookings(package_id);
CREATE INDEX idx_bookings_date ON bookings(checkin_date, checkout_date);
CREATE INDEX idx_packages_category ON packages(category);
CREATE INDEX idx_destinations_category ON destinations(category);
CREATE INDEX idx_destinations_featured ON destinations(featured);
CREATE INDEX idx_reviews_package_id ON reviews(package_id);
CREATE INDEX idx_reviews_destination_id ON reviews(destination_id);

-- Create views for commonly used queries
CREATE VIEW booking_details AS
SELECT 
    b.id as booking_id,
    b.first_name,
    b.last_name,
    b.email,
    b.phone,
    b.checkin_date,
    b.checkout_date,
    b.guests,
    b.total_amount,
    b.booking_date,
    b.status as booking_status,
    p.title as package_title,
    p.destination,
    p.duration,
    p.category
FROM bookings b
JOIN packages p ON b.package_id = p.id;

CREATE VIEW package_stats AS
SELECT 
    p.id,
    p.title,
    p.category,
    p.price,
    COUNT(b.id) as total_bookings,
    AVG(r.rating) as average_rating,
    COUNT(r.id) as total_reviews
FROM packages p
LEFT JOIN bookings b ON p.id = b.package_id
LEFT JOIN reviews r ON p.id = r.package_id
GROUP BY p.id, p.title, p.category, p.price;