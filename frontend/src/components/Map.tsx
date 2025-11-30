import { useEffect, useState, useCallback } from 'react';
import { APIProvider, Map, AdvancedMarker, Pin, useMap } from '@vis.gl/react-google-maps';
import type { WazaiMapItem } from '@/types/api';

interface MapComponentProps {
  events: WazaiMapItem[];
  selectedEvent: WazaiMapItem | null;
  onSelectEvent: (event: WazaiMapItem) => void;
}

const GOOGLE_MAPS_API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY || '';
const MAP_ID = 'DEMO_MAP_ID'; // Replace with your Map ID or leave as is for default style

export function MapComponent({ events, selectedEvent, onSelectEvent }: MapComponentProps) {
  // Default center: Taipei 101
  const defaultCenter = { lat: 25.0330, lng: 121.5654 };

  return (
    <div className="absolute inset-0 z-0">
      <APIProvider apiKey={GOOGLE_MAPS_API_KEY}>
        <Map
          defaultCenter={defaultCenter}
          defaultZoom={9}
          mapId={MAP_ID}
          gestureHandling={'greedy'}
          disableDefaultUI={true}
          className="w-full h-full"
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
        <AdvancedMarker
          key={event.id}
          position={{ lat: event.coordinates.latitude, lng: event.coordinates.longitude }}
          onClick={() => onSelectEvent(event)}
        >
          <Pin 
            background={selectedEvent?.id === event.id ? '#2563eb' : '#ef4444'} 
            borderColor={'#ffffff'} 
            glyphColor={'#ffffff'}
            scale={selectedEvent?.id === event.id ? 1.2 : 1}
          />
        </AdvancedMarker>
      ))}
    </>
  );
}
