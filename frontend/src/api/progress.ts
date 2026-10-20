import { api } from './client';
import type { Progress } from '../types';

export async function getProgress(): Promise<Progress> {
  const res = await api.get<Progress>('/api/me/progress');
  return res.data;
}