import { useState } from 'react';
import useSWR from 'swr';
import { Sidebar } from '@/components/Sidebar';
import { MapComponent } from '@/components/Map';
import { DetailPanel } from '@/components/DetailPanel';
import { searchEvents } from '@/services/api';
import type { SearchParams, WazaiMapItem } from '@/types/api';

function App() {
  const [searchParams, setSearchParams] = useState<SearchParams>({
    keyword: '',
    country: 'ALL',
    provider: 'ALL'
  });
  const [selectedEvent, setSelectedEvent] = useState<WazaiMapItem | null>(null);

  const { data: events = [], isLoading } = useSWR(
    ['/api/search', searchParams], 
    ([, params]) => searchEvents(params),
    {
      keepPreviousData: true,
      revalidateOnFocus: false
    }
  );

  const handleSearch = (params: SearchParams) => {
    setSearchParams(params);
    setSelectedEvent(null); // Clear selection on new search
  };

  const handleSelectEvent = (event: WazaiMapItem) => {
    setSelectedEvent(event);
  };

  return (
    <div className="relative w-full h-screen overflow-hidden bg-zinc-50">
      {/* Map Layer */}
      <MapComponent 
        events={events} 
        selectedEvent={selectedEvent} 
        onSelectEvent={handleSelectEvent} 
      />

      {/* Floating UI Layers */}
      <Sidebar 
        onSearch={handleSearch} 
        results={events} 
        onSelectEvent={handleSelectEvent}
        selectedEventId={selectedEvent?.id}
      />

      <DetailPanel 
        event={selectedEvent} 
        onClose={() => setSelectedEvent(null)} 
      />
      
      {/* Loading Indicator (optional, overlaid) */}
      {isLoading && (
        <div className="absolute top-4 left-1/2 -translate-x-1/2 z-50 bg-background/80 backdrop-blur px-4 py-2 rounded-full shadow-sm border text-sm">
          Loading events...
        </div>
      )}
    </div>
  );
}

export default App;