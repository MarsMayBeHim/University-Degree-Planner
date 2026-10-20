import type { ReactNode } from 'react';
import { useDraggable } from '@dnd-kit/core';

export type DragCourse = {
  courseId: number;
  code: string;
  title: string;
  credits: number;
};

type Props = {
  course: DragCourse;
  origin: string;
  className?: string;
  children: ReactNode;
};

export default function DraggableCourse({ course, origin, className = '', children }: Props) {
  const { attributes, listeners, setNodeRef, isDragging } = useDraggable({
    id: `${origin}-${course.courseId}`,
    data: { course },
  });

  return (
    <div
      ref={setNodeRef}
      {...listeners}
      {...attributes}
      className={`${className} ${isDragging ? 'dragging' : ''}`}
    >
      {children}
    </div>
  );
}