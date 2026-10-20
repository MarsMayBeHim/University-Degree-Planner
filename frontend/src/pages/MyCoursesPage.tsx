import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { searchCourses } from '../api/courses';
import { getMyCourses, removeMyCourse, saveMyCourse } from '../api/studentCourses';
import { useDebounce } from '../hooks/useDebounce';

export default function MyCoursesPage() {
  const [text, setText] = useState('');
  const q = useDebounce(text, 300);
  const queryClient = useQueryClient();

  const results = useQuery({ queryKey: ['courses', q], queryFn: () => searchCourses(q) });
  const mine = useQuery({ queryKey: ['myCourses'], queryFn: getMyCourses });

  // After any change, refetch everything that depends on the student's courses
  const refresh = () => {
    queryClient.invalidateQueries({ queryKey: ['myCourses'] });
    queryClient.invalidateQueries({ queryKey: ['eligibility'] });
    queryClient.invalidateQueries({ queryKey: ['progress'] });
  };

  const save = useMutation({ mutationFn: saveMyCourse, onSuccess: refresh });
  const remove = useMutation({ mutationFn: removeMyCourse, onSuccess: refresh });

  const myCourses = mine.data ?? [];
  const completed = myCourses.filter((c) => c.status === 'COMPLETED');
  const completedIds = new Set(completed.map((c) => c.courseId));
  const completedCredits = completed.reduce((sum, c) => sum + c.credits, 0);

  return (
    <>
      <h1>My Courses</h1>
      <p>
        <strong>{completed.length}</strong> courses completed ·{' '}
        <strong>{completedCredits}</strong> credits
      </p>

      <div className="layout">
        <section>
          <h2>Add courses</h2>
          <input
            placeholder="Search by code or title"
            value={text}
            onChange={(e) => setText(e.target.value)}
          />
          <div style={{ marginTop: '0.75rem' }}>
            {results.isLoading && <p>Loading...</p>}
            {results.isError && <p className="warn">Could not load courses.</p>}
            {results.data?.map((c) => (
              <div key={c.id} className="card row">
                <span>
                  <strong>{c.code}</strong> · {c.credits} cr
                  <div>{c.title}</div>
                </span>
                {completedIds.has(c.id) ? (
                  <span className="ok">Completed</span>
                ) : (
                  <button
                    disabled={save.isPending}
                    onClick={() => save.mutate({ courseId: c.id, status: 'COMPLETED' })}
                  >
                    Mark completed
                  </button>
                )}
              </div>
            ))}
          </div>
        </section>

        <section>
          <h2>Completed</h2>
          {mine.isLoading && <p>Loading...</p>}
          {mine.isError && <p className="warn">Could not load your courses.</p>}
          {!mine.isLoading && completed.length === 0 && <p>Nothing marked completed yet.</p>}
          {completed.map((c) => (
            <div key={c.courseId} className="card row">
              <span>
                <strong>{c.code}</strong> · {c.credits} cr
                <div>{c.title}</div>
              </span>
              <button disabled={remove.isPending} onClick={() => remove.mutate(c.courseId)}>
                Remove
              </button>
            </div>
          ))}
        </section>
      </div>
    </>
  );
}