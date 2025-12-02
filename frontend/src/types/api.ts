export type WazaiEventType = 
  | 'TECH_MEETUP' 
  | 'CONFERENCE' 
  | 'TECH_CONFERENCE' 
  | 'WORKSHOP' 
  | 'COMMUNITY_GATHERING' 
  | 'STUDY_GROUP' 
  | 'HACKATHON';

export type WazaiDataSource =
  | 'CONNPASS'
  | 'TAIWAN_TECH_COMMUNITY'
  | 'AWS_EVENTS'
  | 'GOOGLE_COMMUNITY'
  | 'MEETUP'
  | 'TECHPLAY'
  | 'UNKNOWN';

export type WazaiCountry = 'TAIWAN' | 'JAPAN' | 'DEFAULT';

export interface Coordinates {
  latitude: number;
  longitude: number;
}

export interface WazaiMapItem {
  id: string;
  title: string;
  description: string;
  url: string;
  coordinates: Coordinates;
  startTime?: string;
  endTime?: string;
  eventType?: WazaiEventType;
  source: WazaiDataSource;
  country: WazaiCountry;
}

export interface SearchParams {
  keyword?: string;
  country?: 'ALL' | 'TW' | 'JP';
  provider?: string;
}
