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