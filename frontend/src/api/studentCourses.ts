import { api } from './client';
import type { CourseStatus, StudentCourse } from '../types';

export type SaveCourseRequest = {
  courseId: number;
  status: CourseStatus;
  plannedYear?: number;
  plannedTerm?: string;
  clearSchedule?: boolean;
};

export async function getMyCourses(): Promise<StudentCourse[]> {
  const res = await api.get<StudentCourse[]>('/api/me/courses');
  return res.data;
}

export async function saveMyCourse(body: SaveCourseRequest): Promise<StudentCourse> {
  const res = await api.post<StudentCourse>('/api/me/courses', body);
  return res.data;
}

export async function removeMyCourse(courseId: number): Promise<void> {
  await api.delete(`/api/me/courses/${courseId}`);
}