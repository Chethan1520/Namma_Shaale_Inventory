# Namma-Shaale: Digital Asset Auditor & Inventory Management System

**Namma-Shaale Inventory** is a robust, professional Android application developed to solve the critical challenge of tracking government-funded assets in educational institutions. Built using modern Android standards, it ensures transparency, accountability, and resource optimization.

---

## 📋 Project Overview
The system acts as a "Digital Auditor" for schools, allowing teachers and administrators to maintain a real-time record of lab equipment, sports kits, and classroom infrastructure. It transitions schools from error-prone paper logs to a secure, cloud-ready digital platform.

### 🌟 Problem Statement
Public schools receive vast amounts of resources, but "Health Checks" are rarely performed. Broken or lost items often go unnoticed for months. **Namma-Shaale** provides a simplified, high-speed auditing tool to ensure every taxpayer-funded resource is functional and ready for student use.

---

## 🚀 Key Features & Functionality

### 🔐 1. Secure Authentication System
*   **User Lifecycle:** Full Register -> Login -> Logout flow.
*   **Data Isolation:** Multi-user support where each user's data is strictly private. A Principal/Teacher only sees the institutions and assets they have registered.
*   **Account Recovery:** Secure "Forgot Password" feature using a localized **Security Question** mechanism.

### 🏫 2. Institution-Centric Management
*   **Multi-School Support:** Manage multiple schools or departments under a single user account.
*   **Onboarding:** Streamlined setup for registering new institutions with zero data overlap.

### 🔍 3. Smart Asset Audit (Computer Vision)
*   **Barcode/QR Scanning:** Integrated **Google ML Kit** to scan serial numbers and tags directly via the camera or gallery, eliminating manual entry errors.
*   **Photographic Evidence:** Uses **CameraX** to document the physical condition of high-value assets during registration and audits.

### 📈 4. Real-Time Analytics & Reporting
*   **Health Dashboard:** Visual charts (Jetpack Compose Canvas) showing the percentage of "Working," "Needs Repair," and "Broken" items.
*   **Official PDF Export:** One-click generation of professional audit reports including statistics, budget estimates, and detailed inventory logs.
*   **Audit History:** A complete historical timeline for every asset, showing every check-up and condition change.

---

## 🛠️ Technical Stack (The "How it's Built")

### **Frontend (UI/UX)**
*   **Framework:** Jetpack Compose (100% Declarative UI).
*   **Navigation:** Compose Navigation component for type-safe screen transitions.
*   **Animations:** Smooth transitions used in the **Splash Screen** and loading states.

### **Backend & Storage**
*   **Database:** Room Persistence Library (SQLite abstraction).
*   **Architecture:** **MVVM (Model-View-ViewModel)** with Repository pattern for clean separation of concerns.
*   **Data Handling:** Kotlin **Coroutines and Flow** for reactive, non-blocking UI updates.

### **Integrations**
*   **Vision:** Google ML Kit (Barcode Scanning).
*   **Imaging:** CameraX Jetpack Library.
*   **PDF Engine:** Android Graphics `PdfDocument` API.
*   **Image Loading:** Coil (Coroutines Image Loader).

---

## 📂 System Architecture (Internal Logic)

### **Navigation Flow**
1.  **Splash Screen:** Animated logo entry.
2.  **Authentication:** Login or Create Account.
3.  **Dashboard:** High-level overview of asset health.
4.  **Institution Manager:** Select or register a specific school.
5.  **Asset Manager:** Register new items or perform audits.
6.  **Reporting:** View analytics and export PDFs.

### **Data Privacy Logic**
The database is structured using a **Relational Schema (Version 7)** where:
*   `User` 1:N `Institutions` (Linked via email).
*   `Institution` 1:N `Assets` (Linked via institution ID).
*   `Asset` 1:N `HealthChecks` (Linked via asset ID).
*   **Strit Privacy:** All queries are filtered by the current user's session to ensure 100% data isolation.

---

## 🎯 Impact & Success Criteria
*   **Efficiency:** Monthly audits of 10+ items can be completed in under 2 minutes.
*   **Scalability:** Supports thousands of assets across multiple institutions.
*   **Accountability:** Provides a "Digital Paper Trail" for government auditors.

---
*This project was developed as a Final Year Internship Project for the MindMatrix VTU Internship Program.*
