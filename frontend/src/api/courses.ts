import { api } from './client';
import type { Course, Eligibility } from '../types';

export async function searchCourses(q: string): Promise<Course[]> {
  const res = await api.get<Course[]>('/api/courses', { params: { q } });
  return res.data;
}

export async function getEligibility(id: number): Promise<Eligibility> {
  const res = await api.get<Eligibility>(`/api/courses/${id}/eligibility`);
  return res.data;
}