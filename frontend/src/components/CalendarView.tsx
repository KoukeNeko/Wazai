import { useMemo, useState } from 'react';
import { Calendar } from '@/components/ui/calendar';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import type { WazaiMapItem } from '@/types/api';

interface CalendarViewProps {
  events: WazaiMapItem[];
  onSelectEvent: (event: WazaiMapItem) => void;
  selectedEventId?: string;
}

const getTimezoneOffset = (country: string): string => {
  switch (country) {
    case 'JAPAN':
      return '+09:00';
    case 'TAIWAN':
      return '+08:00';
    default:
      return '';
  }
};

const parseEventDate = (dateTimeStr: string, country: string): Date => {
  return new Date(dateTimeStr + getTimezoneOffset(country));
};

const formatTime = (dateTimeStr: string, country: string): string => {
  const date = parseEventDate(dateTimeStr, country);
  const timezone = country === 'JAPAN' ? 'Asia/Tokyo' : country === 'TAIWAN' ? 'Asia/Taipei' : undefined;
  return date.toLocaleTimeString('ja-JP', {
    timeZone: timezone,
    hour: '2-digit',
    minute: '2-digit'
  });
};

const isSameDay = (date1: Date, date2: Date): boolean => {
  return (
    date1.getFullYear() === date2.getFullYear() &&
    date1.getMonth() === date2.getMonth() &&
    date1.getDate() === date2.getDate()
  );
};

export function CalendarView({ events, onSelectEvent, selectedEventId }: CalendarViewProps) {
  const [selectedDate, setSelectedDate] = useState<Date | undefined>(new Date());

  const eventDates = useMemo(() => {
    const dates = new Set<string>();
    events.forEach(event => {
      if (event.startTime) {
        const date = parseEventDate(event.startTime, event.country);
        dates.add(date.toDateString());
      }
    });
    return dates;
  }, [events]);

  const eventsOnSelectedDate = useMemo(() => {
    if (!selectedDate) return [];
    return events.filter(event => {
      if (!event.startTime) return false;
      const eventDate = parseEventDate(event.startTime, event.country);
      return isSameDay(eventDate, selectedDate);
    }).sort((a, b) => {
      const dateA = parseEventDate(a.startTime!, a.country).getTime();
      const dateB = parseEventDate(b.startTime!, b.country).getTime();
      return dateA - dateB;
    });
  }, [events, selectedDate]);

  const modifiers = useMemo(() => ({
    hasEvent: (date: Date) => eventDates.has(date.toDateString())
  }), [eventDates]);

  const modifiersStyles = {
    hasEvent: {
      fontWeight: 'bold' as const,
      textDecoration: 'underline',
      textDecorationColor: 'hsl(var(--primary))',
      textUnderlineOffset: '4px'
    }
  };

  return (
    <div className="flex flex-col h-full">
      <div className="flex justify-center py-2">
        <Calendar
          mode="single"
          selected={selectedDate}
          onSelect={setSelectedDate}
          modifiers={modifiers}
          modifiersStyles={modifiersStyles}
          className="rounded-md border"
        />
      </div>

      <div className="flex-1 min-h-0">
        <div className="px-4 py-2 text-sm font-medium text-muted-foreground">
          {selectedDate ? (
            <>
              {selectedDate.toLocaleDateString('ja-JP', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                weekday: 'short'
              })}
              <span className="ml-2 text-foreground">
                ({eventsOnSelectedDate.length} events)
              </span>
            </>
          ) : (
            'Select a date'
          )}
        </div>

        <ScrollArea className="flex-1 px-2">
          <div className="space-y-2 pb-4">
            {eventsOnSelectedDate.length === 0 ? (
              <div className="text-center text-muted-foreground py-8 text-sm">
                No events on this date
              </div>
            ) : (
              eventsOnSelectedDate.map(event => (
                <Card
                  key={event.id}
                  className={`cursor-pointer transition-all hover:shadow-md ${
                    selectedEventId === event.id
                      ? 'ring-2 ring-primary shadow-md'
                      : 'hover:bg-accent/50'
                  }`}
                  onClick={() => onSelectEvent(event)}
                >
                  <CardContent className="p-3 space-y-1">
                    <div className="flex justify-between items-start gap-2">
                      <h4 className="font-medium text-sm leading-tight line-clamp-2">
                        {event.title}
                      </h4>
                      <Badge variant="outline" className="shrink-0 text-xs">
                        {event.startTime && formatTime(event.startTime, event.country)}
                      </Badge>
                    </div>
                    <div className="flex gap-2 text-xs">
                      <Badge variant="secondary" className="text-xs">
                        {event.source.replace(/_/g, ' ')}
                      </Badge>
                      <span className="text-muted-foreground">{event.country}</span>
                    </div>
                  </CardContent>
                </Card>
              ))
            )}
          </div>
        </ScrollArea>
      </div>
    </div>
  );
}
