# ERRORCab
### Indian Cab Ride Booking & Tracking System
**Team Name:** ERROR  
**Tagline:** *"Book Smart. Ride Safe."*

---

## 1. Project Overview

**ERRORCab** is a complete, runnable, mobile-friendly Indian cab ride booking and simulated GPS tracking application built for college **Object-Oriented Programming (OOP)** vivas, project presentations, and demonstrations.

Built with **Java 21**, **Spring Boot 3**, **SQLite 3**, and modern **Vanilla HTML5/CSS3/JavaScript**, the system runs **100% locally and offline** without requiring any paid third-party APIs, Google Maps API keys, cloud databases, or external payment gateways.

### Key Highlights
- **Cross-Platform & Mobile Friendly**: Works smoothly on Desktop browsers and on any Android/iOS smartphone connected to the same Wi-Fi network (`http://<COMPUTER-IP>:8080`).
- **Primary Demonstration Region**: Kerala / Kochi / Ernakulam (Kakkanad Infopark, Edappally Toll, Vyttila Mobility Hub, Aluva Metro, Fort Kochi, MG Road, Thrippunithura, Kaloor, Palarivattom, Ernakulam South).
- **Indian Localization**:
  - All currencies in Indian Rupee (**₹**).
  - Authentic mobile numbers (`+91 XXXXX XXXXX`).
  - Realistic Kerala vehicle plates (`KL 07 AB 1234`, `KL 39 C 4567`, `KL 01 AX 7821`, `KL 41 D 9087`).
  - Popular Indian car models: Maruti Suzuki Dzire, Hyundai Aura, Maruti Suzuki Ertiga, Toyota Etios.
- **Three Focused Roles**:
  1. **Passenger**: Quick cab booking, live GPS-style route simulation, timeline tracking, trip history, demo payments (Cash, UPI, Card), promo codes, favorite places, and driver ratings.
  2. **Driver**: Online/offline toggle, incoming request modal with accept/reject, step-by-step ride flow (Arrived ➔ Start Ride with OTP ➔ Complete Ride), single active ride constraint, and earnings dashboard.
  3. **Admin**: Fleet statistics, passenger/driver activation toggles, revenue aggregates, and system audit logs.
- **Safety & Emergency Features**:
  - **🚨 SOS Emergency Alert (Demo)**: Instantly dispatches trip details to local Kerala emergency hotlines (Police 112, Women Helpline 1091, Ambulance 108).
  - **🔗 Share Ride Details**: 1-click clipboard copy of live ride link and vehicle telemetry.

---

## 2. Technology Stack

| Component | Technology | Purpose |
| :--- | :--- | :--- |
| **Backend Framework** | Spring Boot 3.2.3 (Java 21) | RESTful API, embedded high-performance Tomcat server |
| **Language** | Java (OpenJDK 21) | Core business logic, polymorphism, state machine |
| **Database** | SQLite 3 (`errorcab.db`) | Local persistent relational database (auto-seeded) |
| **Connectivity** | JDBC (`sqlite-jdbc` 3.45.1.0) | PreparedStatements, relational schema & transactions |
| **Frontend** | HTML5, CSS3, Vanilla JavaScript | Responsive mobile-first design, interactive HTML5 canvas |
| **Telemetry / Map** | HTML5 Canvas (`map-canvas.js`) | Simulated GPS route tracking, smooth cab animation |
| **Build Tool** | Apache Maven 3.9.6 | Dependency resolution and standalone JAR packaging |

---

## 3. Demo Accounts & Credentials

For fast evaluation and grading during college viva sessions, **1-click quick-fill demo buttons** are embedded directly on the Login screen (`/login.html`):

| Role | Email | Password | Details |
| :--- | :--- | :--- | :--- |
| **Passenger** | `passenger@example.com` | `password123` | Rahul Nair (Kakkanad, Kochi) |
| **Driver** | `driver@example.com` | `password123` | Akhil Raj (Hyundai Aura, `KL 07 AB 1234`) |
| **Admin** | `admin@example.com` | `password123` | Kerala Operations Admin |

---

## 4. How to Run the Application

### Option A: Using the 1-Click Launchers (Recommended)
Double-click either:
- **`run.bat`** (Windows Command Prompt)
- **`run.ps1`** (Windows PowerShell)

Both scripts will automatically detect Java 21, show your computer's local Wi-Fi IP address, and launch the Spring Boot server on port `8080`.

### Option B: Using Maven or Java CLI
Open a terminal in the project directory:

```powershell
# Set Java and Maven environment (if not in system PATH)
$env:JAVA_HOME = "C:\Users\azelh\tools\jdk-21.0.12.1+1"
$env:PATH = "$env:JAVA_HOME\bin;C:\Users\azelh\tools\apache-maven-3.9.6\bin;$env:PATH"

# Run via packaged Spring Boot JAR:
java -jar target/errorcab-1.0.0.jar

# Or run via Maven:
mvn spring-boot:run
```

### Accessing the Web Application
- **On your Computer / Laptop**:
  Open your browser and visit: `http://localhost:8080`
- **On your Mobile Phone (Android / iPhone)**:
  Connect your phone to the same Wi-Fi network as your computer, and open:
  `http://<YOUR-COMPUTER-IP>:8080` (e.g., `http://10.232.119.115:8080`)

---

## 5. Cab Categories & Polymorphic Pricing

| Category | Base Fare | Per KM Rate | Minimum Fare | Capacity & Features |
| :--- | :---: | :---: | :---: | :--- |
| **ECONOMY** | ₹50 | ₹14 | ₹80 | 4 Seats (Alto, WagonR, Dzire) • Everyday city travel |
| **PREMIUM** | ₹80 | ₹20 | ₹120 | 4 Seats (Aura, Etios) • AC • Top-rated drivers |
| **SUV** | ₹100 | ₹25 | ₹150 | 6-7 Seats (Ertiga, Innova) • Large boot space |

### Fare Formula (Pure Java Polymorphism)
$$\text{Fare} = \max(\text{Base Fare} + (\text{Distance} \times \text{Per KM Rate}), \text{Minimum Fare})$$
*Final fare is rounded to the nearest ₹1.*

**Example: Kakkanad Infopark → Vyttila Mobility Hub (9.2 km)**:
- **Economy**: $\max(50 + (9.2 \times 14), 80) = 50 + 128.8 = 178.8 \rightarrow \mathbf{₹179}$
- **Premium**: $\max(80 + (9.2 \times 20), 120) = 80 + 184 = \mathbf{₹264}$
- **SUV**: $\max(100 + (9.2 \times 25), 150) = 100 + 230 = \mathbf{₹330}$

### Promo Codes (Local Java Engine)
- `WELCOME` — Flat **₹50 discount** on orders above ₹100.
- `ERROR50` — Special Team ERROR **₹50 discount** on orders above ₹120.
- `FIRST50` — First ride promo of **₹50 discount** across Kerala.

---

## 6. OOP Concepts Demonstrated (Viva-Ready Guide)

During a college viva or evaluation, you can present each OOP pillar with concrete code references:

### 1. Encapsulation
- **Where**: Classes in `com.errorcab.model.*` (`User`, `Passenger`, `Driver`, `Vehicle`, `Booking`, `Payment`, `Rating`).
- **How**: All fields are declared `private`. Access and mutations are governed via validated getters/setters (e.g., rating clamped to range $[1, 5]$, non-negative fare checks, password hashing).
- **Benefit**: Maintains data integrity and prevents corrupt state from entering SQLite.

### 2. Inheritance
- **Where**:
  - `User` (`model/User.java`, abstract base class) extended by:
    - `Passenger` (`model/Passenger.java`)
    - `Driver` (`model/Driver.java`)
    - `Admin` (`model/Admin.java`)
  - `Cab` (`model/Cab.java`, abstract base class) extended by:
    - `EconomyCab` (`model/EconomyCab.java`)
    - `PremiumCab` (`model/PremiumCab.java`)
    - `SUVCab` (`model/SUVCab.java`)
- **Benefit**: Eliminates duplicate fields (name, email, phone) and centralizes shared behaviors.

### 3. Polymorphism
- **Where**:
  - `Cab.calculateFare(double distanceKm)` is an abstract method overridden differently in `EconomyCab`, `PremiumCab`, and `SUVCab`. The caller (`FareService`) invokes `cab.calculateFare(distance)` without hardcoded `if-else` branches.
  - `User.getWelcomeSubtitle()` dynamically resolves role-specific greetings.
- **Benefit**: Open/Closed Principle (easily add new cab types like Electric Cab `EV_CAB` without modifying existing fare logic).

### 4. Abstraction
- **Where**:
  - Abstract classes `User` and `Cab` define the interface and contracts.
  - Separation of layers: Presentation layer (`resources/static/*.html`) interacts only through HTTP REST controllers (`controller/*`), which delegate to business services (`service/*`), which communicate through repositories (`repository/*`) to SQLite.

---

## 7. Ride State Machine & Driver Constraints

ERRORCab enforces a strict state machine:

```
[SEARCHING] ➔ [DRIVER_ASSIGNED] ➔ [DRIVER_ARRIVING] ➔ [RIDE_STARTED] ➔ [RIDE_COMPLETED]
     │                 │
     └─────────────────┴──────────➔ [CANCELLED]
```

- **Cancellation Rules**: A passenger can only cancel while status is `SEARCHING` or `DRIVER_ASSIGNED`. Once the driver is arriving or the ride has started, cancellation is locked.
- **Single Active Ride Constraint**: `DriverService.acceptRide()` strictly checks that a driver cannot accept a new trip if they already have an active uncompleted trip (`DRIVER_ASSIGNED`, `DRIVER_ARRIVING`, or `RIDE_STARTED`).

---

## 8. Web Application Pages Directory

| Page | URL | Description |
| :--- | :--- | :--- |
| **Landing** | `/index.html` | Hero section, features, quick sign-in links |
| **Login** | `/login.html` | 1-click viva demo credentials for Passenger, Driver, Admin |
| **Register** | `/register.html` | Tabbed signup form for Passengers and Driver partners |
| **Passenger Dashboard** | `/passenger-dashboard.html` | Total trips, amount spent (₹), active trip pulse, saved places, recent history |
| **Booking** | `/booking.html` | 5-step interactive booking flow, canvas route preview, live fares, promo codes |
| **Live Tracking** | `/tracking.html` | Simulated GPS HTML5 Canvas, driver card, timeline, SOS, payment, 5-star rating |
| **Trip History** | `/history.html` | Past rides, filter pills, search, printable official tax invoice receipt |
| **Driver Dashboard** | `/driver-dashboard.html` | Online/offline toggle, incoming request modal, stage advancement (Arrived ➔ Start ➔ Complete) |
| **Admin Operations** | `/admin-dashboard.html` | Platform KPIs, fleet list, user suspension toggles, full ride audit log |
| **User Profile** | `/profile.html` | Profile overview, favorite places manager (Home/Work), emergency contact settings |

---

## 9. Viva Voce Q&A Cheat Sheet

1. **Q: Why is Spring Boot used instead of Node.js or React?**  
   *A:* The core requirement for this college OOP project was to build the business logic in pure Java. Spring Boot provides an embedded web server and robust REST architecture while allowing the entire object-oriented domain model to be implemented in Java 21.

2. **Q: How does the simulated GPS map work without Google Maps API keys?**  
   *A:* An HTML5 `<canvas>` element renders authentic Kochi landmark coordinates (Kakkanad, Vyttila, Edappally, etc.), connecting roads, backwaters, and an animated vehicle marker with trigonometric heading rotation using quadratic bezier route curves.

3. **Q: How is the database kept local?**  
   *A:* SQLite is used via `sqlite-jdbc`. The database file `errorcab.db` lives directly in the project root directory. On first startup, `DatabaseSeeder` automatically creates all tables and populates sample drivers, vehicles, promo codes, and bookings.

4. **Q: How does the driver single active ride constraint work?**  
   *A:* In `DriverService.acceptRide()`, a check queries all active bookings assigned to the driver. If any booking is in progress (`DRIVER_ASSIGNED`, `DRIVER_ARRIVING`, or `RIDE_STARTED`), the system rejects the acceptance with an `IllegalStateException`.
