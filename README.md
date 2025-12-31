# Wazai

A location-based tech event aggregator for engineers in Japan and Taiwan. Built with Spring Boot, React, and Google Maps API.

<img width="2416" height="1327" alt="image" src="https://github.com/user-attachments/assets/9eec2b08-8a0a-4eea-ac7c-31dc1509c841" />

## Overview

Wazai aggregates tech events from multiple platforms and displays them on an interactive map, helping developers discover meetups, conferences, and workshops in their area.

### Key Features

- **Multi-Source Aggregation**: Connpass, Meetup, GDG Community, AWS Events, Doorkeeper, TechPlay
- **Interactive Map**: Google Maps with custom markers, dark/light themes
- **Advanced Search**: Filter by keyword, country (Japan/Taiwan), and provider
- **Real-time Updates**: Live event data from 7+ sources

## Tech Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Core language |
| Spring Boot | 4.0.0 | Web framework |
| Gradle | 9.x | Build tool |
| Spring WebFlux | - | Async HTTP client |
| SpringDoc OpenAPI | - | API documentation |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| React | 19.x | UI framework |
| TypeScript | 5.9 | Type safety |
| Vite | 7.x | Build tool |
| Tailwind CSS | 3.4 | Styling |
| @vis.gl/react-google-maps | - | Map integration |

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Frontend                              │
│  React + TypeScript + Tailwind CSS + Google Maps            │
└─────────────────────────┬───────────────────────────────────┘
                          │ REST API
┌─────────────────────────▼───────────────────────────────────┐
│                        Backend                               │
│  Spring Boot + WebFlux                                       │
│  ┌─────────────────────────────────────────────────────────┐│
│  │                  WazaiSearchService                      ││
│  │                    (Orchestrator)                        ││
│  └─────────────────────────┬───────────────────────────────┘│
│                            │                                 │
│  ┌─────────┬─────────┬─────┴────┬──────────┬──────────┐     │
│  │Connpass │ Meetup  │   GDG    │   AWS    │Doorkeeper│     │
│  │Provider │Provider │ Provider │ Provider │ Provider │     │
│  └────┬────┴────┬────┴────┬─────┴────┬─────┴────┬─────┘     │
└───────┼─────────┼─────────┼──────────┼──────────┼───────────┘
        │         │         │          │          │
        ▼         ▼         ▼          ▼          ▼
   [Connpass] [Meetup] [GDG API] [AWS API] [Doorkeeper]
      API       API                           API
```

## Getting Started

### Prerequisites
- Java 21+
- Node.js 20+
- Google Maps API Key

### Backend Setup
```bash
cd backend
cp .env.example .env
# Edit .env with your API keys
./gradlew bootRun
```

### Frontend Setup
```bash
cd frontend
cp .env.example .env
# Add VITE_GOOGLE_MAPS_API_KEY
npm install
npm run dev
```

### Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `GOOGLE_MAPS_API_KEY` | Yes | Google Maps Geocoding API |
| `CONNPASS_API_TOKEN` | No | Connpass API access |
| `DOORKEEPER_API_TOKEN` | No | Doorkeeper API access |
| `POSITIONSTACK_API_KEY` | No | Fallback geocoding |

## API Documentation

Swagger UI available at `http://localhost:8080/swagger-ui.html`

### Main Endpoint
```
GET /api/search?keyword=python&country=JP&provider=connpass
```

## Project Structure

```
wazai/
├── backend/
│   └── src/main/java/dev/koukeneko/wazai/
│       ├── controller/       # REST endpoints
│       ├── service/          # Business logic
│       │   └── impl/         # Provider implementations
│       ├── dto/              # Data transfer objects
│       └── config/           # Spring configuration
└── frontend/
    └── src/
        ├── components/       # React components
        ├── services/         # API client
        └── types/            # TypeScript definitions
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License.
