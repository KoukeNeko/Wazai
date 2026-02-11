import { useEffect, useMemo } from 'react';
import Map, { useMap, MapProvider } from 'react-map-gl/maplibre';
import 'maplibre-gl/dist/maplibre-gl.css';
import type { WazaiMapItem } from '@/types/api';
import { useTheme } from '@/components/theme-provider';
import { OverlayMarker } from '@/components/OverlayMarker';

interface MapComponentProps {
  events: WazaiMapItem[];
  selectedEvent: WazaiMapItem | null;
  onSelectEvent: (event: WazaiMapItem) => void;
}

const DARK_STYLE = 'https://basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json';
const LIGHT_STYLE = 'https://basemaps.cartocdn.com/gl/positron-gl-style/style.json';

export function MapComponent(props: MapComponentProps) {
  const { theme } = useTheme();
  
  const isDark = useMemo(() => {
    if (theme === 'dark') return true;
    if (theme === 'light') return false;
    return window.matchMedia('(prefers-color-scheme: dark)').matches;
  }, [theme]);

  // Default center: Taipei 101
  const defaultCenter = { lat: 25.0330, lng: 121.5654 };

  return (
    <div className="absolute inset-0 z-0">
      <MapProvider>
        <Map
            id="mainMap"
            initialViewState={{
                longitude: defaultCenter.lng,
                latitude: defaultCenter.lat,
                zoom: 9
            }}
            style={{width: '100%', height: '100%'}}
            mapStyle={isDark ? DARK_STYLE : LIGHT_STYLE}
            attributionControl={false}
        >
            <Markers {...props} />
        </Map>
      </MapProvider>
    </div>
  );
}

function Markers({ events, selectedEvent, onSelectEvent }: MapComponentProps) {
  const { mainMap } = useMap();

  useEffect(() => {
    if (!mainMap || !selectedEvent) return;

    mainMap.flyTo({
      center: [selectedEvent.coordinates.longitude, selectedEvent.coordinates.latitude],
      zoom: 15,
      essential: true
    });
    
  }, [mainMap, selectedEvent]);

  const getEventColor = (event: WazaiMapItem) => {
    if (event.title.toLowerCase().includes('sitcon') || event.id.toLowerCase().includes('sitcon')) {
      return '#77B55A'; // SITCON Green
    }
    if (event.title.toLowerCase().includes('coscup') || event.id.toLowerCase().includes('coscup')) {
      return '#3B9837'; // COSCUP Green
    }
    if (event.title.toLowerCase().includes('hitcon') || event.id.toLowerCase().includes('hitcon')) {
      return '#1E1E1E'; // HITCON Dark
    }
    if (event.title.toLowerCase().includes('mopcon') || event.id.toLowerCase().includes('mopcon')) {
      return '#060000'; // MOPCON Black
    }
    if (event.title.toLowerCase().includes('gdg') || event.id.toLowerCase().includes('gdg') || 
        event.title.toLowerCase().includes('devfest') || event.source === 'GOOGLE_COMMUNITY') {
      return '#1973E8'; // GDG Blue
    }
    if (event.title.toLowerCase().includes('agile') || event.id.toLowerCase().includes('agile')) {
      return '#FE8938'; // Agile Summit Orange
    }
    if (event.source === 'AWS_EVENTS') {
      return '#FF9900'; // AWS Orange
    }
    if (event.source === 'TECHPLAY') {
      return '#062145'; // TechPlay Navy
    }
    if (event.source === 'DOORKEEPER') {
      return '#124FF4'; // Doorkeeper Blue
    }
    return '#ef4444'; // Default Red (Tailwind red-500)
  };

  return (
    <>
      {events.map((event) => {
        const isSelected = selectedEvent?.id === event.id;
        const color = getEventColor(event);
        
        return (
          <OverlayMarker
            key={event.id}
            position={{ lat: event.coordinates.latitude, lng: event.coordinates.longitude }}
            zIndex={isSelected ? 100 : 1}
          >
            <div 
              className="relative flex h-4 w-4 items-center justify-center cursor-pointer group"
              onClick={(e) => {
                e.preventDefault(); 
                e.stopPropagation();
                onSelectEvent(event);
              }}
            >
              <span 
                className="animate-ping-slow absolute inline-flex h-full w-full rounded-full opacity-75"
                style={{ backgroundColor: color }}
              ></span>
              
              <span 
                className={`relative inline-flex rounded-full h-3 w-3 border-2 border-white shadow-sm transition-transform duration-300 ${isSelected ? 'scale-150' : 'group-hover:scale-125'}`}
                style={{ backgroundColor: color }}
              ></span>
            </div>
          </OverlayMarker>
        );
      })}
    </>
  );
}
