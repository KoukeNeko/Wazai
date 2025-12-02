# Wazai 🗾

> A location-based tech event aggregator for engineers in Taiwan and Japan.

![Demo Screenshot](https://github.com/user-attachments/assets/9eec2b08-8a0a-4eea-ac7c-31dc1509c841)

## 🛠 Tech Stack

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![TypeScript](https://img.shields.io/badge/TypeScript-007ACC?style=for-the-badge&logo=typescript&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)
![Google Maps API](https://img.shields.io/badge/Google_Maps-4285F4?style=for-the-badge&logo=google-maps&logoColor=white)

## 🚀 Features

- **Event Aggregation**: Centralizes tech meetups from Taiwan, Japan, and international sources, solving the fragmentation problem.
- **Map Visualization**: Interactive Google Maps integration to find events near you.
- **Cross-Region Coverage**: Seamlessly integrates events from multiple regions into a single view.

## 💡 Development Process

**The Problem:**
Tech event information in Taiwan and Japan is often scattered across different platforms, making it difficult to track what's happening nearby or across regions.

**The Solution:**
I built a full-stack application to aggregate this data and visualize it geographically.

1. **Backend**: Constructed a robust REST API using **Spring Boot (Java 21)** to manage event data, built with **Gradle** for reliable dependency management.
2. **Frontend**: Built a modern, type-safe UI using **React**, **TypeScript**, and **Tailwind CSS** for rapid, responsive styling.
3. **Integration**: Integrated **Google Maps API** to provide the core location-based discovery feature.

## 🧠 What I Learned

- **Full Stack Architecture**: Orchestrating data flow between a Java/Spring Boot backend and a React frontend.
- **Modern Build Tools**: Migrating to **Vite** for faster frontend development and using **Gradle** for backend builds.
- **Component Design**: Leveraging Headless UI libraries (Radix UI) to build accessible and interactive map components.
- **API Challenges**: Efficiently handling Google Maps API quotas and rendering custom markers.

## 🔮 Future Improvements

- [ ] **User Auth**: Add authentication to allow users to save/bookmark events.
- [ ] **Automated Scraping**: Implement jobs to scrape more event platforms.
- [ ] **Advanced Filters**: Add filtering capabilities for specific programming languages or dates.

## 🏃‍♂️ How to Run

### Prerequisites
- Java 21
- Node.js & npm
- Google Maps API Key

### 1. Backend (Spring Boot)
```bash
cd backend
# The project uses Gradle wrapper
./gradlew bootRun
```

### 2. Frontend (React + Vite)
```bash
cd frontend
npm install

# Create a .env file in the frontend directory
# Add: VITE_GOOGLE_MAPS_API_KEY=your_api_key_here

npm run dev
```
