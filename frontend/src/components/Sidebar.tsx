import { useState, useEffect, useMemo } from 'react';
import { Search, MapPin, ArrowUpDown } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import type { WazaiMapItem, SearchParams } from '@/types/api';
import { getProviders } from '@/services/api';
import { ModeToggle } from '@/components/mode-toggle';

interface SidebarProps {
  onSearch: (params: SearchParams) => void;
  results: WazaiMapItem[];
  onSelectEvent: (event: WazaiMapItem) => void;
  selectedEventId?: string;
}

export function Sidebar({ onSearch, results, onSelectEvent, selectedEventId }: SidebarProps) {
  const [keyword, setKeyword] = useState('');
  const [country, setCountry] = useState<'ALL' | 'TW' | 'JP'>('ALL');
  const [provider, setProvider] = useState('ALL');
  const [providerList, setProviderList] = useState<string[]>([]);
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');

  useEffect(() => {
    getProviders().then(setProviderList).catch(console.error);
  }, []);

  const sortedResults = useMemo(() => {
    return [...results].sort((a, b) => {
      // If no start time, put at the end for ascending sort, or at the beginning for descending?
      // Usually items without date should be at the bottom regardless.
      const dateA = a.startTime ? new Date(a.startTime).getTime() : (sortOrder === 'asc' ? Number.MAX_SAFE_INTEGER : 0);
      const dateB = b.startTime ? new Date(b.startTime).getTime() : (sortOrder === 'asc' ? Number.MAX_SAFE_INTEGER : 0);
      
      return sortOrder === 'asc' ? dateA - dateB : dateB - dateA;
    });
  }, [results, sortOrder]);

  const handleSearch = () => {
    onSearch({ keyword, country, provider });
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  return (
    <Card className="absolute left-4 top-4 bottom-4 w-96 z-10 flex flex-col shadow-xl bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 border-border/40">
      <CardHeader className="pb-4 flex flex-row items-center justify-between space-y-0">
        <CardTitle className="text-xl font-bold flex items-center gap-2">
          <MapPin className="h-6 w-6 text-primary" />
          Wazai Maps
        </CardTitle>
        <ModeToggle />
      </CardHeader>
      
      <CardContent className="space-y-4">
        <div className="flex gap-2">
          <Input 
            placeholder="Search events..." 
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onKeyDown={handleKeyDown}
            className="flex-1"
          />
          <Button onClick={handleSearch} size="icon">
            <Search className="h-4 w-4" />
          </Button>
        </div>

        <div className="flex gap-2">
          <Select value={country} onValueChange={(v: any) => setCountry(v)}>
            <SelectTrigger className="w-[140px]">
              <SelectValue placeholder="Country" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">All Countries</SelectItem>
              <SelectItem value="TW">Taiwan</SelectItem>
              <SelectItem value="JP">Japan</SelectItem>
            </SelectContent>
          </Select>

          <Select value={provider} onValueChange={setProvider}>
            <SelectTrigger className="flex-1">
              <SelectValue placeholder="Provider" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">All Providers</SelectItem>
              {providerList.map((p) => (
                <SelectItem key={p} value={p}>{p}</SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="flex items-center justify-between">
          <span className="text-sm text-muted-foreground">{results.length} results</span>
          <Button 
            variant="ghost" 
            size="sm" 
            className="h-8 text-xs"
            onClick={() => setSortOrder(prev => prev === 'asc' ? 'desc' : 'asc')}
          >
            {sortOrder === 'asc' ? 'Earliest First' : 'Latest First'}
            <ArrowUpDown className="h-3 w-3 ml-2" />
          </Button>
        </div>
      </CardContent>

      <Separator />

      <ScrollArea className="flex-1">
        <div className="p-4 space-y-3">
          {sortedResults.length === 0 ? (
            <div className="text-center text-muted-foreground py-8">
              No events found. Try searching or changing filters.
            </div>
          ) : (
            sortedResults.map((event) => (
              <Card 
                key={event.id} 
                className={`cursor-pointer transition-all hover:bg-accent/50 ${selectedEventId === event.id ? 'border-primary bg-accent/50' : ''}`}
                onClick={() => onSelectEvent(event)}
              >
                <CardContent className="p-4 space-y-2">
                  <div className="flex justify-between items-start gap-2">
                    <h3 className="font-semibold leading-tight">{event.title}</h3>
                    <Badge variant="secondary" className="shrink-0 text-xs">
                      {event.source.replace(/_/g, ' ')}
                    </Badge>
                  </div>
                  <p className="text-sm text-muted-foreground line-clamp-2">
                    {event.description}
                  </p>
                  <div className="text-xs text-muted-foreground flex gap-2 mt-2">
                    <span>{event.startTime ? new Date(event.startTime).toLocaleDateString() : 'No Date'}</span>
                    <span>•</span>
                    <span>{event.country}</span>
                  </div>
                </CardContent>
              </Card>
            ))
          )}
        </div>
      </ScrollArea>
    </Card>
  );
}
