import { api } from './client';
import type { GenerationResult, Plan } from '../types';

export async function getPlan(): Promise<Plan> {
  const res = await api.get<Plan>('/api/me/plan');
  return res.data;
}

export async function generatePlan(
  minCredits: number,
  maxCredits: number,
  from?: { year: number; term: string },
): Promise<GenerationResult> {
  const res = await api.post<GenerationResult>('/api/me/plan/generate', {
    minCredits,
    maxCredits,
    fromYear: from?.year,
    fromTerm: from?.term,
  });
  return res.data;
}