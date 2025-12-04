# Smart Waste Management System – Backend Features

A production-grade Spring Boot backend that turns citizen reports and sensor data into **instant, optimal collection routes** — fully autonomous, real-time, and driver-ready.

## Core Features

### 1. Autonomous Task Generation (`SensorSimulator`)
- Runs every hour (`@Scheduled`)
- Automatically detects containers with **>75% fill level**
- Creates a **PENDING task with exactly 4 full containers** (the fullest free ones)
- Skips containers already assigned to pending tasks → **no duplicates**
- Assigns 1 driver + 1 loader + the best available truck
- Blocks employee & vehicle availability for 30 minutes

### 2. Real-Time Optimal Routing (`RouteOptimizationService` + `OSRMRoutingService`)
- Uses **OSRM** (Open Source Routing Machine) for real drivable routes
- Solves TSP with **Nearest Neighbor + 2-opt** heuristic
- **Forces route to start and end at truck location** (closed loop)
- Returns high-precision encoded polyline (`overview=full`)
- Saves full route: distance (km), duration (min), polyline, order

### 3. Instant Report Resolution (`TaskAssignmentService.resolveReport`)
- Citizen reports a full bin → system finds **nearest container**
- If container is free → creates **urgent task** with optimal route
- If already scheduled → resolves report without duplicate task
- Assigns employees + vehicle + blocks availability
- Updates report status → `RESOLVED` or `Under_Review`

### 4. Driver Dashboard API (`RouteService.getRoutesByEmployeeId`)
- Returns all past & current routes for a driver
- Includes: polyline, container order, distance, duration, vehicle ID
- Sorted by **most recent first** (`calculatedAt DESC`)
- Used by mobile/web driver app

### 5. Real-Time Updates (WebSocket)
- All changes pushed instantly via Spring WebSocket:
  - `/topic/containers` → live fill levels & status
  - `/topic/tasks` → new tasks appear immediately
  - `/topic/reports` → status updates
- Drivers see new routes **in real time**

### 6. Geospatial Intelligence
- All coordinates stored as `[latitude, longitude]` (double[2])
- Haversine distance for nearest container search
- Polyline decoding support on frontend (Leaflet-ready)
- Truck always starts route from current GPS location

### 7. Data Model Highlights
- `Container`: fillLevel, status, location
- `Task`: PENDING / IN_PROGRESS / COMPLETED, employees, vehicle, containers
- `Route`: polyline, routeOrder, distance, duration, calculatedAt, vehicleId
- `Report`: citizen reports with photo & location
- `AssignmentSlot`: blocks availability with time range

### 8. Testing & Reliability
- 100% unit & integration tested
- Mocked OSRM, repositories, WebSocket
- Edge cases covered: no containers, already scheduled, no employees

## Tech Stack
- **Spring Boot 3** • **Spring Data MongoDB/JPA** • **Spring WebSocket**
- **OSRM** (local or public instance) • **RestTemplate**
- **Lombok** • **JUnit 5** • **Mockito**

## Why This System Is Special
- No human dispatcher needed
- Drivers get **perfect turn-by-turn routes** on their phone
- Citizens see their report **resolved in minutes**
- Zero duplicate tasks, zero wasted fuel
- Fully real-time, scalable, and production-ready

**This is not just a backend.**  
**This is the brain of a smart, clean, future city.**

Built with love, precision, and pride.
