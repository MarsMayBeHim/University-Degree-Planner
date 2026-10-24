import { useState } from 'react';
import {
  DndContext,
  DragOverlay,
  KeyboardSensor,
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
import type { PlannedCourse, Semester } from '../types';

function storedNumber(key: string, fallback: number): number {
  const value = Number(localStorage.getItem(key));
  return Number.isFinite(value) && value > 0 ? value : fallback;
}

const semesterKey = (s: Semester) => `${s.year}-${s.term}`;

export default function RoadmapPage() {
  const [text, setText] = useState('');
  const [active, setActive] = useState<DragCourse | null>(null);
  const [minCredits, setMinCredits] = useState(() => storedNumber('plan-min-credits', 12));
  const [maxCredits, setMaxCredits] = useState(() => storedNumber('plan-max-credits', 16));
  const [fromKey, setFromKey] = useState<string | null>(null);
  const q = useDebounce(text, 300);
  const queryClient = useQueryClient();

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
  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } }),
    useSensor(KeyboardSensor),
  );

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
    mutationFn: (from: { year: number; term: string }) =>
      generatePlan(minCredits, maxCredits, from),
    onSuccess: refresh,
  });

  function handleDragStart(e: DragStartEvent) {
    setActive((e.active.data.current?.course as DragCourse | undefined) ?? null);
  }

  function handleDragEnd(e: DragEndEvent) {
    setActive(null);
    const course = e.active.data.current?.course as DragCourse | undefined;
    const target = e.over?.data.current as { year: number; term: string } | undefined;
    if (!course || !target) return;
    // Completed and in-progress courses keep their status, everything else becomes planned
    save.mutate({
      courseId: course.courseId,
      status: course.status ?? 'PLANNED',
      plannedYear: target.year,
      plannedTerm: target.term,
    });
  }

  function handleRemove(course: PlannedCourse) {
    if (course.status === 'PLANNED') {
      remove.mutate(course.courseId);
    } else {
      // Keep the course as completed or in progress, only take it out of the semester
      save.mutate({ courseId: course.courseId, status: course.status, clearSchedule: true });
    }
  }

  function handleToggle(course: PlannedCourse, year: number, term: string) {
    save.mutate({
      courseId: course.courseId,
      status: course.status === 'COMPLETED' ? 'PLANNED' : 'COMPLETED',
      plannedYear: year,
      plannedTerm: term,
    });
  }

  if (plan.isLoading) return <p>Loading...</p>;
  if (plan.isError || !plan.data) return <p className="warn">Could not load your plan.</p>;

  const semesters = plan.data.semesters;

  // Generation starts after the latest semester that holds a completed or in-progress course
  const lastTakenIndex = semesters.reduce(
    (last, s, i) => (s.courses.some((c) => c.status !== 'PLANNED') ? i : last),
    -1,
  );
  const defaultFrom = lastTakenIndex + 1;
  const chosenKey = fromKey ?? (defaultFrom < semesters.length ? semesterKey(semesters[defaultFrom]) : '');
  const fromSemester = semesters.find((s) => semesterKey(s) === chosenKey);

  const lastUsed = semesters.reduce((last, s, i) => (s.courses.length > 0 ? i : last), -1);

  // Courses in a semester are hidden from the lists, and completed ones without a semester wait in a tray
  const mineIds = new Set(mine.data?.map((c) => c.courseId));
  const tray = (mine.data ?? []).filter((c) => c.status !== 'PLANNED' && c.plannedYear === null);
  const available = results.data?.filter((c) => !mineIds.has(c.id)) ?? [];

  const allCourses = semesters.flatMap((s) => s.courses);
  const completedCredits = allCourses
    .filter((c) => c.status === 'COMPLETED')
    .reduce((sum, c) => sum + c.credits, 0);
  const plannedCredits = allCourses
    .filter((c) => c.status === 'PLANNED')
    .reduce((sum, c) => sum + c.credits, 0);

  // Group the 8 semesters into academic years of two
  const years: Semester[][] = [];
  for (let i = 0; i < semesters.length; i += 2) {
    years.push(semesters.slice(i, i + 2));
  }

  return (
    <>
      <h1>Four-Year Roadmap</h1>
      <p>
        Drag courses into a semester, and drag cards between semesters to move them. Put courses
        you have already taken in the semester you took them, then generate the rest.
      </p>
      <p>
        <strong>{completedCredits}</strong> credits completed in the roadmap ·{' '}
        <strong>{plannedCredits}</strong> credits planned
      </p>

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
        <label>
          Generate starting from{' '}
          <select value={chosenKey} onChange={(e) => setFromKey(e.target.value)}>
            {semesters.map((s) => (
              <option key={semesterKey(s)} value={semesterKey(s)}>
                {s.label}
              </option>
            ))}
          </select>
        </label>
        <button
          disabled={generate.isPending || !limitsValid || !fromSemester}
          onClick={() => {
            if (!fromSemester) return;
            const ok = window.confirm(
              `This replaces every course you have planned, and starts from ${fromSemester.label}. ` +
                'Completed and in-progress courses stay where they are. Continue?',
            );
            if (ok) {
              generate.mutate({ year: fromSemester.year, term: fromSemester.term });
            }
          }}
        >
          {generate.isPending ? 'Generating...' : 'Generate My Degree Plan'}
        </button>

        {!fromSemester && (
          <p className="warn">
            Every semester already has completed courses. Move some earlier, or pick a start.
          </p>
        )}
        {!limitsValid && (
          <p className="warn">
            The minimum must not exceed the maximum, and the maximum must be between 3 and 24.
          </p>
        )}
        {generate.isError && (
          <p className="warn">
            Could not generate a plan. Check that there is room left after your completed courses.
          </p>
        )}
        {generate.data && (
          <div>
            <p className="ok">
              Planned {generate.data.coursesPlaced} courses from {generate.data.startLabel} across{' '}
              {generate.data.semestersUsed} semesters.
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
            {tray.length > 0 && (
              <>
                <h2>Completed</h2>
                <p>
                  <small>Drag each one into the semester you took it.</small>
                </p>
                {tray.map((c) => (
                  <DraggableCourse
                    key={c.courseId}
                    course={c}
                    origin="tray"
                    className="plan-card completed"
                  >
                    <div className="plan-card-top">
                      <strong>{c.code}</strong>
                      <span>{c.credits} cr</span>
                    </div>
                    <div className="plan-card-title">{c.title}</div>
                    <div className="small ok">
                      {c.status === 'COMPLETED' ? 'Completed' : 'In progress'}
                    </div>
                  </DraggableCourse>
                ))}
              </>
            )}

            <h2>Courses</h2>
            <input
              placeholder="Search by code or title"
              value={text}
              onChange={(e) => setText(e.target.value)}
            />
            <p>
              <small>Courses already in a semester are hidden.</small>
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
                      key={semesterKey(s)}
                      semester={s}
                      min={minCredits}
                      max={maxCredits}
                      light={i * 2 + j < lastUsed && s.credits < minCredits}
                      onRemove={handleRemove}
                      onToggleCompleted={handleToggle}
                    />
                  ))}
                </div>
              </div>
            ))}
          </section>
        </div>

        <DragOverlay>
          {active ? (
            <div
              className={`plan-card overlay-card ${active.status === 'COMPLETED' ? 'completed' : ''}`}
            >
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