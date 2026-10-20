import { useEffect, useState } from 'react';
import type { ReactNode } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { getMe, loginUser, registerUser } from '../api/auth';
import type { AuthResponse } from '../api/auth';
import { tokenStore } from '../api/client';
import type { User } from '../types';
import { AuthContext } from './AuthContext';

export default function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(tokenStore.get() !== null);

  // Restore the session when the page loads
  useEffect(() => {
    if (!tokenStore.get()) {
      return;
    }
    getMe()
      .then(setUser)
      .catch(() => tokenStore.clear())
      .finally(() => setLoading(false));
  }, []);

  function accept(res: AuthResponse) {
    tokenStore.set(res.token);
    queryClient.clear();
    setUser(res.user);
  }

  const value = {
    user,
    loading,
    login: async (email: string, password: string) => accept(await loginUser(email, password)),
    register: async (email: string, password: string) =>
      accept(await registerUser(email, password)),
    logout: () => {
      tokenStore.clear();
      queryClient.clear();
      setUser(null);
    },
    setUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}