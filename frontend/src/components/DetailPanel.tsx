import { X, Calendar, MapPin, ExternalLink } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Separator } from '@/components/ui/separator';
import type { WazaiMapItem } from '@/types/api';

interface DetailPanelProps {
  event: WazaiMapItem | null;
  onClose: () => void;
}

const getTimezone = (country: string): string => {
  switch (country) {
    case 'JAPAN':
      return 'Asia/Tokyo';
    case 'TAIWAN':
      return 'Asia/Taipei';
    default:
      return Intl.DateTimeFormat().resolvedOptions().timeZone;
  }
};

const formatDateTime = (dateTimeStr: string, country: string): string => {
  const timezone = getTimezone(country);
  const date = new Date(dateTimeStr + getTimezoneOffset(country));
  return date.toLocaleString('ja-JP', {
    timeZone: timezone,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }) + ` (${timezone.split('/')[1]})`;
};

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

export function DetailPanel({ event, onClose }: DetailPanelProps) {
  if (!event) return null;

  return (
    <Card className="absolute right-4 top-4 bottom-4 w-[400px] z-10 flex flex-col shadow-xl bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 border-border/40 animate-in slide-in-from-right-5 duration-300">
      <CardHeader className="pb-2 flex flex-row items-start justify-between space-y-0">
        <CardTitle className="text-xl font-bold leading-snug pr-8">
          {event.title}
        </CardTitle>
        <Button variant="ghost" size="icon" onClick={onClose} className="h-8 w-8 shrink-0">
          <X className="h-4 w-4" />
        </Button>
      </CardHeader>

      <ScrollArea className="flex-1">
        <CardContent className="space-y-6 py-4">

          <div className="flex flex-wrap gap-2">
            <Badge variant="default" className="bg-primary/10 text-primary hover:bg-primary/20 border-none">
              {event.eventType?.replace(/_/g, ' ')}
            </Badge>
            <Badge variant="outline">
              {event.source.replace(/_/g, ' ')}
            </Badge>
            <Badge variant="secondary">
              {event.country}
            </Badge>
          </div>

          <div className="space-y-4 text-sm">
            {event.startTime && (
              <div className="flex items-center gap-3 text-muted-foreground">
                <Calendar className="h-4 w-4 shrink-0" />
                <div>
                  <div className="font-medium text-foreground">Start Time</div>
                  {formatDateTime(event.startTime, event.country)}
                </div>
              </div>
            )}

            {event.endTime && (
              <div className="flex items-center gap-3 text-muted-foreground">
                <Calendar className="h-4 w-4 shrink-0 opacity-0" /> {/* Spacer */}
                <div>
                  <div className="font-medium text-foreground">End Time</div>
                  {formatDateTime(event.endTime, event.country)}
                </div>
              </div>
            )}

            <div className="flex items-start gap-3 text-muted-foreground">
              <MapPin className="h-4 w-4 shrink-0 mt-1" />
              <div>
                <div className="font-medium text-foreground">Location</div>
                {event.address ? (
                  <div className="space-y-1">
                    <div>{event.address}</div>
                    <div className="text-xs opacity-60">
                      {`${event.coordinates.latitude.toFixed(4)}, ${event.coordinates.longitude.toFixed(4)}`}
                    </div>
                  </div>
                ) : (
                  `${event.coordinates.latitude}, ${event.coordinates.longitude}`
                )}
              </div>
            </div>
          </div>

          <Separator />

          <div>
            <h4 className="font-semibold mb-2">About this event</h4>
            <p className="text-sm text-muted-foreground whitespace-pre-wrap leading-relaxed">
              {event.description}
            </p>
          </div>

          <Button className="w-full" asChild>
            <a href={event.url} target="_blank" rel="noopener noreferrer">
              Visit Website
              <ExternalLink className="ml-2 h-4 w-4" />
            </a>
          </Button>

        </CardContent>
      </ScrollArea>
    </Card>
  );
}
