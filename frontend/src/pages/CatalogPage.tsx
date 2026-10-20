import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getEligibility, searchCourses } from '../api/courses';
import { useDebounce } from '../hooks/useDebounce';

export default function CatalogPage() {
  const [text, setText] = useState('');
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const q = useDebounce(text, 300);

  const courses = useQuery({
    queryKey: ['courses', q],
    queryFn: () => searchCourses(q),
  });

  const eligibility = useQuery({
    queryKey: ['eligibility', selectedId],
    queryFn: () => getEligibility(selectedId!),
    enabled: selectedId !== null,
  });

  const selected = courses.data?.find((c) => c.id === selectedId);

  return (
    <>
      <h1>Course Catalog</h1>
      <input
        placeholder="Search by code or title, e.g. CS 211 or algorithms"
        value={text}
        onChange={(e) => setText(e.target.value)}
      />

      <div className="layout">
        <section>
          {courses.isLoading && <p>Loading...</p>}
          {courses.isError && <p className="warn">Could not load courses.</p>}
          {courses.data?.length === 0 && <p>No courses found.</p>}
          {courses.data?.map((c) => (
            <div
              key={c.id}
              className={`card ${c.id === selectedId ? 'selected' : ''}`}
              onClick={() => setSelectedId(c.id)}
            >
              <strong>{c.code}</strong> · {c.credits} credits
              <div>{c.title}</div>
            </div>
          ))}
        </section>

        <section className="panel">
          {!selected && <p>Select a course to see its details.</p>}
          {selected && (
            <>
              <h2>{selected.code}</h2>
              <p>{selected.title}</p>
              <p>{selected.credits} credits</p>
              <h3>Prerequisites</h3>
              <p>{selected.prereqText ?? 'None listed.'}</p>
              {eligibility.isLoading && <p>Checking eligibility...</p>}
              {eligibility.data && (
                <p className={eligibility.data.eligible ? 'ok' : 'warn'}>
                  {eligibility.data.eligible
                    ? 'You can take this course now.'
                    : `Missing: ${eligibility.data.unmetCourses.join(', ')}`}
                </p>
              )}
            </>
          )}
        </section>
      </div>
    </>
  );
}