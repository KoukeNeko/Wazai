import { Marker } from "react-map-gl/maplibre";
import type { ReactNode } from "react";

interface OverlayMarkerProps {
  position: { lat: number; lng: number };
  children: ReactNode;
  zIndex?: number;
}

export function OverlayMarker({
  position,
  children,
  zIndex = 0,
}: OverlayMarkerProps) {
  return (
    <Marker
      longitude={position.lng}
      latitude={position.lat}
      style={{ zIndex }}
      anchor="center"
    >
      {children}
    </Marker>
  );
}
