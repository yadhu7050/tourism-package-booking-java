# Booking Modal Project

## Overview
This project consists of two parts:

1. **Swing Application** – Demo Working in SWING. This version is fully functional and connected to the database.  
2. **Static Website** – Main Project. It is  connected to database and works.

---

## Database Configuration
Before running the Swing project, make sure to configure your database connection.

In the file **`database.properties`**, update the following details:
```bash
username = root
password = your_password
```
Replace `yourpassword` with your actual MySQL root password.

---

## Initial Setup

Before running the program for the first time, set up the database schema by running the following command in PowerShell or your terminal:

```bash
Get-Content -Raw "database-schema.sql" | mysql -u root -p
```
This will prompt you to enter the password and after that it will create the necessary tables and data in your MySQL database.

## Running the Project

Once the database is set up, follow these steps to compile and run the Swing application:

Compile the Java files:

```bash
javac -cp ".;mysql-connector-j-9.1.0.jar" *.java
```
To Run the swing application:

```bash
java -cp ".;mysql-connector-j-9.1.0.jar" BookingModalDemo

```

To run the website :
```bash
java -cp ".;mysql-connector-j-9.1.0.jar" CompatServer 
```
CompatServer will be running on http://localhost:9999
Open: http://localhost:9999/index.html
