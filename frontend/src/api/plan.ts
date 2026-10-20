import { api } from './client';
import type { Plan } from '../types';

export async function getPlan(): Promise<Plan> {
  const res = await api.get<Plan>('/api/me/plan');
  return res.data;
}