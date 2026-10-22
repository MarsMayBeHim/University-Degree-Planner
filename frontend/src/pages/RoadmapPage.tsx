import { useState } from 'react';
import {
  DndContext,
  DragOverlay,
  PointerSensor,
  useSensor,
  useSensors,
} from '@dnd-kit/core';
import type { DragEndEvent, DragStartEvent } from '@dnd-kit/core';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { searchCourses } from '../api/courses';
import { generatePlan, getPlan } from '../api/plan';
import { getMyCourses, removeMyCourse, saveMyCourse } from '../api/studentCourses';
import DraggableCourse from '../components/DraggableCourse';
import type { DragCourse } from '../components/DraggableCourse';
import SemesterColumn from '../components/SemesterColumn';
import { useDebounce } from '../hooks/useDebounce';

export default function RoadmapPage() {
  const [text, setText] = useState('');
  const [active, setActive] = useState<DragCourse | null>(null);
  const q = useDebounce(text, 300);
  const queryClient = useQueryClient();
  const [minCredits, setMinCredits] = useState(() => storedNumber('plan-min-credits', 12));
  const [maxCredits, setMaxCredits] = useState(() => storedNumber('plan-max-credits', 16));
  const limitsValid =
    minCredits >= 0 && maxCredits >= 3 && maxCredits <= 24 && minCredits <= maxCredits;

  function changeMin(value: number) {
    setMinCredits(value);
    localStorage.setItem('plan-min-credits', String(value));
  }

  function changeMax(value: number) {
    setMaxCredits(value);
    localStorage.setItem('plan-max-credits', String(value));
  }

  // A small drag distance lets plain clicks on buttons still work
  const sensors = useSensors(useSensor(PointerSensor, { activationConstraint: { distance: 5 } }));

  const plan = useQuery({ queryKey: ['plan'], queryFn: getPlan });
  const mine = useQuery({ queryKey: ['myCourses'], queryFn: getMyCourses });
  const results = useQuery({ queryKey: ['courses', q], queryFn: () => searchCourses(q) });

  const refresh = () => {
    queryClient.invalidateQueries({ queryKey: ['plan'] });
    queryClient.invalidateQueries({ queryKey: ['myCourses'] });
    queryClient.invalidateQueries({ queryKey: ['progress'] });
    queryClient.invalidateQueries({ queryKey: ['eligibility'] });
  };

  const save = useMutation({ mutationFn: saveMyCourse, onSuccess: refresh });
  const remove = useMutation({ mutationFn: removeMyCourse, onSuccess: refresh });
  const generate = useMutation({
    mutationFn: () => generatePlan(minCredits, maxCredits),
    onSuccess: refresh,
  });
  function handleDragStart(e: DragStartEvent) {
    setActive((e.active.data.current?.course as DragCourse | undefined) ?? null);
  }

  function storedNumber(key: string, fallback: number): number {
    const value = Number(localStorage.getItem(key));
    return Number.isFinite(value) && value > 0 ? value : fallback;
  }

  function handleDragEnd(e: DragEndEvent) {
    setActive(null);
    const course = e.active.data.current?.course as DragCourse | undefined;
    const target = e.over?.data.current as { year: number; term: string } | undefined;
    if (!course || !target) return;
    save.mutate({
      courseId: course.courseId,
      status: 'PLANNED',
      plannedYear: target.year,
      plannedTerm: target.term,
    });
  }

  if (plan.isLoading) return <p>Loading...</p>;
  if (plan.isError || !plan.data) return <p className="warn">Could not load your plan.</p>;
  const lastUsed = plan.data.semesters.reduce((last, s, i) => (s.courses.length > 0 ? i : last), -1);

  // Courses the student already has (completed, in progress or planned) are hidden from the list
  const mineIds = new Set(mine.data?.map((c) => c.courseId));
  const available = results.data?.filter((c) => !mineIds.has(c.id)) ?? [];

  // Group the 8 semesters into academic years of two
  const years = [];
  for (let i = 0; i < plan.data.semesters.length; i += 2) {
    years.push(plan.data.semesters.slice(i, i + 2));
  }

  return (
    <>
      <h1>Four-Year Roadmap</h1>
      <p>Drag courses into a semester. Drag cards between semesters to move them.</p>

        <div className="panel generator">
        <label>
          Min credits per semester{' '}
          <input
            type="number"
            min={0}
            max={24}
            value={minCredits}
            onChange={(e) => changeMin(Number(e.target.value))}
            style={{ width: '5rem' }}
          />
        </label>
        <label>
          Max credits per semester{' '}
          <input
            type="number"
            min={3}
            max={24}
            value={maxCredits}
            onChange={(e) => changeMax(Number(e.target.value))}
            style={{ width: '5rem' }}
          />
        </label>
        <button
          disabled={generate.isPending || !limitsValid}
          onClick={() => {
            if (window.confirm('This replaces every course you have planned so far. Continue?')) {
              generate.mutate();
            }
          }}
        >
          {generate.isPending ? 'Generating...' : 'Generate My Degree Plan'}
        </button>

        {!limitsValid && (
          <p className="warn">
            The minimum must not exceed the maximum, and the maximum must be between 3 and 24.
          </p>
        )}
        {generate.isError && <p className="warn">Could not generate a plan.</p>}
        {generate.data && (
          <div>
            <p className="ok">
              Placed {generate.data.coursesPlaced} courses across {generate.data.semestersUsed}{' '}
              semesters.
            </p>
            {generate.data.lightSemesters.length > 0 && (
              <>
                <p className="light">
                  These semesters are under your {minCredits}-credit minimum, usually because
                  prerequisites leave nothing else to take:
                </p>
                <ul>
                  {generate.data.lightSemesters.map((s) => (
                    <li key={s.label}>
                      {s.label}: {s.credits} credits
                    </li>
                  ))}
                </ul>
              </>
            )}
            {generate.data.unplaced.length > 0 && (
              <>
                <p className="warn">These could not be placed:</p>
                <ul>
                  {generate.data.unplaced.map((u) => (
                    <li key={u.code}>
                      <strong>{u.code}</strong>: {u.reason}
                    </li>
                  ))}
                </ul>
              </>
            )}
          </div>
        )}
      </div>

      <DndContext sensors={sensors} onDragStart={handleDragStart} onDragEnd={handleDragEnd}>
        <div className="roadmap">
          <aside className="panel sidebar">
            <h2>Courses</h2>
            <input
              placeholder="Search by code or title"
              value={text}
              onChange={(e) => setText(e.target.value)}
            />
            <p>
              <small>Completed and already-planned courses are hidden.</small>
            </p>
            {results.isLoading && <p>Loading...</p>}
            {available.map((c) => {
              const course = { courseId: c.id, code: c.code, title: c.title, credits: c.credits };
              return (
                <DraggableCourse key={c.id} course={course} origin="sidebar" className="plan-card">
                  <div className="plan-card-top">
                    <strong>{c.code}</strong>
                    <span>{c.credits} cr</span>
                  </div>
                  <div className="plan-card-title">{c.title}</div>
                </DraggableCourse>
              );
            })}
          </aside>

          <section>
            {years.map((pair, i) => (
              <div key={i} className="year">
                <h2>Year {i + 1}</h2>
                <div className="semesters">
                  {pair.map((s, j) => (
                    <SemesterColumn
                      key={`${s.year}-${s.term}`}
                      semester={s}
                      min={minCredits}
                      max={maxCredits}
                      light={i * 2 + j < lastUsed && s.credits < minCredits}
                      onRemove={(id) => remove.mutate(id)}
                    />
                  ))}
                </div>
              </div>
            ))}
          </section>
        </div>

        <DragOverlay>
          {active ? (
            <div className="plan-card overlay-card">
              <div className="plan-card-top">
                <strong>{active.code}</strong>
                <span>{active.credits} cr</span>
              </div>
              <div className="plan-card-title">{active.title}</div>
            </div>
          ) : null}
        </DragOverlay>
      </DndContext>

      <p className="note">
        Warnings only check prerequisites against courses you have completed or placed in earlier
        semesters. This planner assumes you already meet all grade and admission requirements.
        Confirm your plan with your advisor.
      </p>
    </>
  );
} 