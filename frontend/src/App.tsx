import { Link, Route, Routes } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { api } from './api/client';
import CatalogPage from './pages/CatalogPage';
import MyCoursesPage from './pages/MyCoursesPage';


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

export default function App() {
  return (
    <>
      <nav>
        <Link to="/">Home</Link>
        <Link to="/catalog">Catalog</Link>
        <Link to="/my-courses">My Courses</Link>
      </nav>
      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/catalog" element={<CatalogPage />} />
          <Route path="/my-courses" element={<MyCoursesPage />} />
        </Routes>
      </main>
    </>
  );
}