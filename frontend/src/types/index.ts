export type Course = {
  id: number;
  code: string;
  title: string;
  credits: number;
  prereqText: string | null;
};

export type Eligibility = {
  courseId: number;
  code: string;
  eligible: boolean;
  prereqText: string | null;
  unmetCourses: string[];
};

export type CourseStatus = 'COMPLETED' | 'IN_PROGRESS' | 'PLANNED';

export type StudentCourse = {
  courseId: number;
  code: string;
  title: string;
  credits: number;
  status: CourseStatus;
  plannedYear: number | null;
  plannedTerm: string | null;
};