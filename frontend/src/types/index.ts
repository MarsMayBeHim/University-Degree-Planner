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

export type GroupProgress = {
  id: number;
  name: string;
  ruleType: 'ALL' | 'CHOOSE_N' | 'MIN_CREDITS';
  done: boolean;
  coursesCompleted: number;
  coursesRequired: number | null;
  creditsEarned: number;
  creditsRequired: number | null;
  completedCourses: string[];
  remainingCourses: string[];
  children: GroupProgress[];
};

export type Progress = {
  programId: number;
  programName: string;
  catalogYear: string;
  totalCreditsRequired: number;
  creditsCompleted: number;
  creditsPlanned: number;
  trackedRequirementsMet: boolean;
  groups: GroupProgress[];
};

export type PlannedCourse = {
  courseId: number;
  code: string;
  title: string;
  credits: number;
  prereqText: string | null;
  missingPrereqs: string[];
};

export type Semester = {
  year: number;
  term: string;
  label: string;
  credits: number;
  courses: PlannedCourse[];
};

export type Plan = {
  startYear: number;
  startTerm: string;
  semesters: Semester[];
};

export type GenerationResult = {
  coursesPlaced: number;
  semestersUsed: number;
  unplaced: { code: string; reason: string }[];
};

export type User = {
  id: number;
  email: string;
  programId: number | null;
  startYear: number | null;
  startTerm: 'FALL' | 'SPRING' | null;
};

export type Program = {
  id: number;
  name: string;
  degreeType: string;
  catalogYear: string;
  totalCredits: number;
};