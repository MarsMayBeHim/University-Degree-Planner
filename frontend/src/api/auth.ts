import { api } from './client';
import type { Program, User } from '../types';

export type AuthResponse = { token: string; user: User };

export async function registerUser(email: string, password: string): Promise<AuthResponse> {
  const res = await api.post<AuthResponse>('/api/auth/register', { email, password });
  return res.data;
}

export async function loginUser(email: string, password: string): Promise<AuthResponse> {
  const res = await api.post<AuthResponse>('/api/auth/login', { email, password });
  return res.data;
}

export async function getMe(): Promise<User> {
  const res = await api.get<User>('/api/me');
  return res.data;
}

export async function updateProfile(body: {
  programId: number;
  startYear: number;
  startTerm: string;
}): Promise<User> {
  const res = await api.put<User>('/api/me/profile', body);
  return res.data;
}

export async function getPrograms(): Promise<Program[]> {
  const res = await api.get<Program[]>('/api/programs');
  return res.data;
}