import { api } from './client';
import type { PrereqCourse } from '../types';

export async function getPrerequisiteMap(programId: number): Promise<PrereqCourse[]> {
  const res = await api.get<PrereqCourse[]>(`/api/programs/${programId}/prerequisites`);
  return res.data;
}