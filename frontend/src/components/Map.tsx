import { useEffect, useState, useCallback, useMemo } from 'react';
import { APIProvider, Map, Marker, useMap } from '@vis.gl/react-google-maps';
import type { WazaiMapItem } from '@/types/api';
import { useTheme } from '@/components/theme-provider';

interface MapComponentProps {
  events: WazaiMapItem[];
  selectedEvent: WazaiMapItem | null;
  onSelectEvent: (event: WazaiMapItem) => void;
}

const GOOGLE_MAPS_API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY || '';

// Uber-like Dark Style
const UBER_DARK_STYLE = [
  {
    "elementType": "geometry",
    "stylers": [{ "color": "#212121" }]
  },
  {
    "elementType": "labels.icon",
    "stylers": [{ "visibility": "off" }]
  },
  {
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#757575" }]
  },
  {
    "elementType": "labels.text.stroke",
    "stylers": [{ "color": "#212121" }]
  },
  {
    "featureType": "administrative",
    "elementType": "geometry",
    "stylers": [{ "color": "#757575" }]
  },
  {
    "featureType": "administrative.country",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#9e9e9e" }]
  },
  {
    "featureType": "administrative.land_parcel",
    "stylers": [{ "visibility": "off" }]
  },
  {
    "featureType": "administrative.locality",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#bdbdbd" }]
  },
  {
    "featureType": "poi",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#757575" }]
  },
  {
    "featureType": "poi.park",
    "elementType": "geometry",
    "stylers": [{ "color": "#181818" }]
  },
  {
    "featureType": "poi.park",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#616161" }]
  },
  {
    "featureType": "poi.park",
    "elementType": "labels.text.stroke",
    "stylers": [{ "color": "#1b1b1b" }]
  },
  {
    "featureType": "road",
    "elementType": "geometry.fill",
    "stylers": [{ "color": "#2c2c2c" }]
  },
  {
    "featureType": "road",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#8a8a8a" }]
  },
  {
    "featureType": "road.arterial",
    "elementType": "geometry",
    "stylers": [{ "color": "#373737" }]
  },
  {
    "featureType": "road.highway",
    "elementType": "geometry",
    "stylers": [{ "color": "#3c3c3c" }]
  },
  {
    "featureType": "road.highway.controlled_access",
    "elementType": "geometry",
    "stylers": [{ "color": "#4e4e4e" }]
  },
  {
    "featureType": "road.local",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#616161" }]
  },
  {
    "featureType": "transit",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#757575" }]
  },
  {
    "featureType": "water",
    "elementType": "geometry",
    "stylers": [{ "color": "#000000" }]
  },
  {
    "featureType": "water",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#3d3d3d" }]
  }
] as google.maps.MapTypeStyle[];

export function MapComponent({ events, selectedEvent, onSelectEvent }: MapComponentProps) {
  // Default center: Taipei 101
  const defaultCenter = { lat: 25.0330, lng: 121.5654 };
  
  const { theme } = useTheme();
  
  const isDark = useMemo(() => {
    if (theme === 'dark') return true;
    if (theme === 'light') return false;
    return window.matchMedia('(prefers-color-scheme: dark)').matches;
  }, [theme]);

  return (
    <div className="absolute inset-0 z-0">
      <APIProvider apiKey={GOOGLE_MAPS_API_KEY}>
        <Map
          defaultCenter={defaultCenter}
          defaultZoom={9}
          gestureHandling={'greedy'}
          disableDefaultUI={true}
          className="w-full h-full"
          styles={isDark ? UBER_DARK_STYLE : []}
        >
          <Markers 
            events={events} 
            selectedEvent={selectedEvent} 
            onSelectEvent={onSelectEvent} 
          />
        </Map>
      </APIProvider>
    </div>
  );
}

// Separate component to use useMap hook
function Markers({ events, selectedEvent, onSelectEvent }: MapComponentProps) {
  const map = useMap();

  useEffect(() => {
    if (!map || !selectedEvent) return;

    map.panTo({
      lat: selectedEvent.coordinates.latitude,
      lng: selectedEvent.coordinates.longitude,
    });
    map.setZoom(15);
  }, [map, selectedEvent]);

  return (
    <>
      {events.map((event) => (
        <Marker
          key={event.id}
          position={{ lat: event.coordinates.latitude, lng: event.coordinates.longitude }}
          onClick={() => onSelectEvent(event)}
        />
      ))}
    </>
  );
}
