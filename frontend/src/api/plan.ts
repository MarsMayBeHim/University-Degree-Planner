import { api } from './client';
import type { GenerationResult, Plan } from '../types';

export async function getPlan(): Promise<Plan> {
  const res = await api.get<Plan>('/api/me/plan');
  return res.data;
}

export async function generatePlan(maxCredits: number): Promise<GenerationResult> {
  const res = await api.post<GenerationResult>('/api/me/plan/generate', { maxCredits });
  return res.data;
}