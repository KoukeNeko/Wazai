# Proposed Plan: Replace Google Maps with MapLibre (MapCN)

## Context

The user wants to replace the current Google Maps implementation in the frontend with `https://www.mapcn.dev`.
Research indicates `mapcn.dev` is likely a wrapper or philosophy around using MapLibre GL with React, similar to `shadcn/ui`.
Since `mapcn.dev` is currently inaccessible, I will implement a standard `react-map-gl` + `maplibre-gl` solution, which is the industry standard open-source alternative to Google Maps and aligns with the "MapCN" description.

## Analysis

- **Current State:**
  - `src/components/Map.tsx`: Uses `@vis.gl/react-google-maps`.
  - `src/components/OverlayMarker.tsx`: Uses `google.maps.OverlayView` via portal.
  - `package.json`: Contains `@vis.gl/react-google-maps`.
- **Target State:**
  - Use `react-map-gl` with `maplibre-gl`.
  - Use CartoDB Basemaps (Dark Matter/Positron) for styling, as they don't require an API key and support dark mode well.

## High-Level Design

1.  **Dependencies**:
    - Remove `@vis.gl/react-google-maps`.
    - Add `react-map-gl`, `maplibre-gl`, `@types/maplibre-gl`.
2.  **Components**:
    - **OverlayMarker**: Rewrite to use `<Marker>` from `react-map-gl`. This will be much simpler as it supports children natively.
    - **Map**: Rewrite to use `<Map>` from `react-map-gl`.
      - Handle Theme switching (Light/Dark style URLs).
      - Handle `onSelectEvent` (markers are interactive).
      - Handle `useMap` hook for panning to selected event.

## Step-by-Step Implementation

1.  Uninstall Google Maps packages and install MapLibre packages.
2.  Refactor `OverlayMarker.tsx`.
3.  Refactor `Map.tsx`.
4.  Remove unused env var reference (optional cleanup).

## Verification Strategy

1.  TypeScript compilation check (`npm run build` or `tsc`).
2.  Verify component structure matches `react-map-gl` usage.
