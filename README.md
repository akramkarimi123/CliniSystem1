# CliniSystem1
This is the java Advance project.


#  Karimi Dental Clinic Management System

A complete desktop application for managing dental clinic operations including patient records, appointments, treatment catalog, and billing/invoicing.

##  Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Installation Guide](#installation-guide)
- [Database Setup](#database-setup)
- [Running the Application](#running-the-application)
- [Login Credentials](#login-credentials)
- [User Roles & Permissions](#user-roles--permissions)
- [Module Guide](#module-guide)
- [Troubleshooting](#troubleshooting)
- [Screenshots](#screenshots)
- [License](#license)

##  Features

### Core Modules

| Module | Description |
|--------|-------------|
| **Patient Management** | Add, update, delete, and search patient records with medical history |
| **Appointment Scheduling** | Schedule, reschedule, cancel, and track appointments |
| **Treatment Catalog** | Manage dental treatments, procedures, and pricing (Admin only) |
| **Billing & Invoicing** | Create invoices, process payments, track balances, print receipts |

### Key Functionalities

-  **Role-Based Access Control** (Admin & Staff roles)
-  **Real-Time Dashboard Statistics** (Appointments, Payments, Patients, Revenue)
-  **Search & Filter** across all modules
-  **Automatic Invoice Number Generation**
-  **Real-Time Balance Calculation** with tax and discount
-  **Responsive UI** with professional clinical design
-  **MySQL Database** with complete data persistence

##  Technology Stack

| Component | Technology |
|-----------|------------|
| Programming Language | Java 26 |
| UI Framework | JavaFX 26 |
| Database | MySQL 8.0 |
| Database Driver | MySQL Connector/J 8.0.33 |
| Build Tool | Manual (javac) |
| IDE Support | VS Code / IntelliJ IDEA / Eclipse |

##  Project Structure
DentalClinicSystem/
│
├── src/
│ ├── Main.java # Application entry point
│ ├── model/ # Data models
│ │ ├── Patient.java
│ │ ├── Appointment.java
│ │ ├── Treatment.java
│ │ └── Billing.java
│ ├── controller/ # Business logic
│ │ ├── LoginController.java
│ │ ├── DashboardController.java
│ │ ├── PatientController.java
│ │ ├── AppointmentController.java
│ │ ├── TreatmentController.java
│ │ └── BillingController.java
│ ├── view/ # FXML UI files
│ │ ├── login.fxml
│ │ ├── dashboard.fxml
│ │ ├── patient_form.fxml
│ │ ├── appointment_form.fxml
│ │ ├── treatment_form.fxml
│ │ └── billing.fxml
│ └── db/ # Database connection
│ └── DatabaseConnection.java
│
├── lib/ # External JARs
│ └── mysql-connector-java-8.0.33.jar
│
├── dental_clinic.sql # Database schema
└── README.md # Documentation




##  Prerequisites

Before running this application, ensure you have the following installed:

| Software | Version | Download Link |
|----------|---------|---------------|
| Java JDK | 17 or higher | [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) / [OpenJDK](https://adoptium.net/) |
| JavaFX SDK | 21 or higher | [Gluon JavaFX](https://gluonhq.com/products/javafx/) |
| MySQL Server | 8.0 or higher | [MySQL Community Server](https://dev.mysql.com/downloads/mysql/) |
| MySQL Connector/J | 8.0.33 | [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) |

## Installation Guide

### Step 1: Install Java JDK

```bash
# Verify installation
java -version
javac -version
