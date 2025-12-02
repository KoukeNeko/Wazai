import axios from 'axios';
import type { SearchParams, WazaiMapItem } from '@/types/api';

const api = axios.create({
  baseURL: '/api',
});

export const searchEvents = async (params: SearchParams = {}): Promise<WazaiMapItem[]> => {
  const { data } = await api.get<WazaiMapItem[]>('/search', { params });
  return data;
};

export const getProviders = async (): Promise<string[]> => {
  const { data } = await api.get<{ providers: string[] }>('/search/providers');
  return data.providers;
};
