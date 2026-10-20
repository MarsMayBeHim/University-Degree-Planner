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
import { getPlan } from '../api/plan';
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

  function handleDragStart(e: DragStartEvent) {
    setActive((e.active.data.current?.course as DragCourse | undefined) ?? null);
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
                  {pair.map((s) => (
                    <SemesterColumn
                      key={`${s.year}-${s.term}`}
                      semester={s}
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