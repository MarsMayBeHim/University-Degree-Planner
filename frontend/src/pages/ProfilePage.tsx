import { useState } from 'react';
import type { FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { getPrograms, updateProfile } from '../api/auth';
import { useAuth } from '../auth/AuthContext';

export default function ProfilePage() {
  const { user, setUser } = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const programs = useQuery({ queryKey: ['programs'], queryFn: getPrograms });
  const [programId, setProgramId] = useState<number | ''>(user?.programId ?? '');
  const [startYear, setStartYear] = useState(user?.startYear ?? new Date().getFullYear());
  const [startTerm, setStartTerm] = useState<string>(user?.startTerm ?? 'FALL');

  const save = useMutation({
    mutationFn: updateProfile,
    onSuccess: (updated) => {
      setUser(updated);
      // The plan and progress depend on these settings
      queryClient.invalidateQueries();
      navigate('/dashboard');
    },
  });

  function submit(e: FormEvent) {
    e.preventDefault();
    if (programId === '') {
      return;
    }
    save.mutate({ programId, startYear, startTerm });
  }

  return (
    <div className="panel auth-box">
      <h1>Your degree</h1>
      <form onSubmit={submit}>
        <label>
          Major
          <select
            required
            value={programId}
            onChange={(e) => setProgramId(e.target.value === '' ? '' : Number(e.target.value))}
          >
            <option value="">Choose a major...</option>
            {programs.data?.map((p) => (
              <option key={p.id} value={p.id}>
                {p.degreeType} {p.name} ({p.catalogYear})
              </option>
            ))}
          </select>
        </label>
        <label>
          First semester
          <div className="inline">
            <select value={startTerm} onChange={(e) => setStartTerm(e.target.value)}>
              <option value="FALL">Fall</option>
              <option value="SPRING">Spring</option>
            </select>
            <input
              type="number"
              min={2000}
              max={2100}
              value={startYear}
              onChange={(e) => setStartYear(Number(e.target.value))}
            />
          </div>
        </label>
        {programs.isError && <p className="warn">Could not load majors.</p>}
        {save.isError && <p className="warn">Could not save your settings.</p>}
        <button type="submit" disabled={save.isPending}>
          Save
        </button>
      </form>
    </div>
  );
}