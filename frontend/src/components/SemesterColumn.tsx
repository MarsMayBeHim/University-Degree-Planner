import { useDroppable } from '@dnd-kit/core';
import type { PlannedCourse, Semester } from '../types';
import DraggableCourse from './DraggableCourse';

type Props = {
  semester: Semester;
  min: number;
  max: number;
  light: boolean;
  onRemove: (course: PlannedCourse) => void;
  onToggleCompleted: (course: PlannedCourse, year: number, term: string) => void;
};

export default function SemesterColumn({
  semester,
  min,
  max,
  light,
  onRemove,
  onToggleCompleted,
}: Props) {
  const { setNodeRef, isOver } = useDroppable({
    id: `${semester.year}-${semester.term}`,
    data: { year: semester.year, term: semester.term },
  });

  // A semester made only of completed courses is history, so it gets no credit warnings
  const historical =
    semester.courses.length > 0 && semester.courses.every((c) => c.status === 'COMPLETED');
  const over = !historical && semester.credits > max;
  const under = !historical && light;

  return (
    <div
      ref={setNodeRef}
      className={`semester ${isOver ? 'over' : ''} ${historical ? 'historical' : ''}`}
    >
      <div className="semester-head">
        <span>{semester.label}</span>
        <span className={over ? 'warn' : under ? 'light' : ''}>{semester.credits} cr</span>
      </div>
      {historical && <small className="ok">Completed</small>}
      {over && <small className="warn">Over the {max}-credit limit</small>}
      {under && <small className="light">Under the {min}-credit minimum</small>}

      {semester.courses.length === 0 && <small>Drop courses here</small>}

      {semester.courses.map((c) => {
        const done = c.status === 'COMPLETED';
        const invalid = !done && c.missingPrereqs.length > 0;
        return (
          <DraggableCourse
            key={c.courseId}
            course={c}
            origin="semester"
            className={`plan-card ${done ? 'completed' : ''} ${invalid ? 'invalid' : ''}`}
          >
            <div className="plan-card-top">
              <strong>{c.code}</strong>
              <span>
                {c.credits} cr
                <button
                  className="icon-btn"
                  title={done ? 'Mark as planned again' : 'Mark as completed'}
                  aria-label={done ? `Mark ${c.code} as planned` : `Mark ${c.code} as completed`}
                  onPointerDown={(e) => e.stopPropagation()}
                  onClick={() => onToggleCompleted(c, semester.year, semester.term)}
                >
                  {done ? '↺' : '✓'}
                </button>
                <button
                  className="icon-btn"
                  title={c.status === 'PLANNED' ? 'Remove from plan' : 'Take out of this semester'}
                  aria-label={`Remove ${c.code}`}
                  onPointerDown={(e) => e.stopPropagation()}
                  onClick={() => onRemove(c)}
                >
                  ✕
                </button>
              </span>
            </div>
            <div className="plan-card-title">{c.title}</div>
            {done && <div className="small ok">Completed</div>}
            {c.status === 'IN_PROGRESS' && <div className="small progress-label">In progress</div>}
            {invalid && (
              <div className="warn small" title={c.prereqText ?? ''}>
                ⚠ Needs earlier: {c.missingPrereqs.join(', ')}
              </div>
            )}
          </DraggableCourse>
        );
      })}
    </div>
  );
}