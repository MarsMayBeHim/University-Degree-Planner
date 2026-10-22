import { useDroppable } from '@dnd-kit/core';
import type { Semester } from '../types';
import DraggableCourse from './DraggableCourse';

type Props = {
  semester: Semester;
  min: number;
  max: number;
  light: boolean;
  onRemove: (courseId: number) => void;
};

export default function SemesterColumn({ semester, min, max, light, onRemove }: Props) {
  const { setNodeRef, isOver } = useDroppable({
    id: `${semester.year}-${semester.term}`,
    data: { year: semester.year, term: semester.term },
  });

  return (
    <div ref={setNodeRef} className={`semester ${isOver ? 'over' : ''}`}>
      <div className="semester-head">
        <span>{semester.label}</span>
        <span className={semester.credits > max ? 'warn' : light ? 'light' : ''}>
          {semester.credits} cr
        </span>
      </div>
      {semester.credits > max && <small className="warn">Over the {max}-credit limit</small>}
      {light && <small className="light">Under the {min}-credit minimum</small>}

      {semester.courses.length === 0 && <small>Drop courses here</small>}

      {semester.courses.map((c) => (
        <DraggableCourse
          key={c.courseId}
          course={c}
          origin="semester"
          className={`plan-card ${c.missingPrereqs.length > 0 ? 'invalid' : ''}`}
        >
          <div className="plan-card-top">
            <strong>{c.code}</strong>
            <span>
              {c.credits} cr
              <button
                className="icon-btn"
                aria-label={`Remove ${c.code}`}
                onPointerDown={(e) => e.stopPropagation()}
                onClick={() => onRemove(c.courseId)}
              >
                ✕
              </button>
            </span>
          </div>
          <div className="plan-card-title">{c.title}</div>
          {c.missingPrereqs.length > 0 && (
            <div className="warn small" title={c.prereqText ?? ''}>
              ⚠ Needs earlier: {c.missingPrereqs.join(', ')}
            </div>
          )}
        </DraggableCourse>
      ))}
    </div>
  );
}