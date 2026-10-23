import type { ReactNode } from 'react';
import { Link, Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { api } from './api/client';
import { useAuth } from './auth/AuthContext';
import CatalogPage from './pages/CatalogPage';
import DashboardPage from './pages/DashboardPage';
import LoginPage from './pages/LoginPage';
import MyCoursesPage from './pages/MyCoursesPage';
import ProfilePage from './pages/ProfilePage';
import RoadmapPage from './pages/RoadmapPage';
import PrerequisiteMapPage from './pages/PrerequisiteMapPage';  

function Home() {
  const health = useQuery({
    queryKey: ['health'],
    queryFn: async () => (await api.get<{ status: string }>('/api/health')).data.status,
  });
  return (
    <>
      <h1>Degree Planner</h1>
      <p>
        Backend status:{' '}
        {health.isLoading ? 'loading...' : health.isError ? 'unreachable' : health.data}
      </p>
    </>
  );
}

function Protected({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) return <p>Loading...</p>;
  if (!user) return <Navigate to="/login" replace />;

  const setupDone = user.programId !== null && user.startYear !== null && user.startTerm !== null;
  if (!setupDone && location.pathname !== '/profile') {
    return <Navigate to="/profile" replace />;
  }
  return <>{children}</>;
}

export default function App() {
  const { user, logout } = useAuth();

  return (
    <>
      <nav>
        <Link to="/">Home</Link>
        {user && (
          <>
            <Link to="/catalog">Catalog</Link>
            <Link to="/my-courses">My Courses</Link>
            <Link to="/dashboard">Dashboard</Link>
            <Link to="/roadmap">Roadmap</Link>
            <Link to="/profile">Profile</Link>
            <Link to="/prerequisites">Prerequisites</Link>
          </>
        )}
        <span className="spacer" />
        {user ? (
          <>
            <span>{user.email}</span>
            <button onClick={logout}>Log out</button>
          </>
        ) : (
          <Link to="/login">Log in</Link>
        )}
      </nav>
      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/profile" element={<Protected><ProfilePage /></Protected>} />
          <Route path="/catalog" element={<Protected><CatalogPage /></Protected>} />
          <Route path="/my-courses" element={<Protected><MyCoursesPage /></Protected>} />
          <Route path="/dashboard" element={<Protected><DashboardPage /></Protected>} />
          <Route path="/roadmap" element={<Protected><RoadmapPage /></Protected>} />
          <Route path="/prerequisites" element={<Protected><PrerequisiteMapPage /></Protected>} />
        </Routes>
      </main>
    </>
  );
}