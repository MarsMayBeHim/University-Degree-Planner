# University Degree Planner
![CI](https://github.com/MarsMayBeHim/university-degree-planner/actions/workflows/ci.yml/badge.svg)

**Plan your whole degree, from first semester to graduation.**

Choose a major, mark the courses you've finished, and see exactly which requirements remain. Then generate a semester-by-semester roadmap that respects prerequisites and your credit limit per semester.

Built for George Mason University students, starting with the BS in Computer Science (2025-2026 catalog).

## Features

- **Degree progress dashboard:** completed and remaining requirements at a glance, with progress bars for each requirement group
- **Course catalog search:** browse courses with credits and prerequisites
- **Eligibility checks:** see which prerequisites you're missing for any course
- **Four-year roadmap:** drag and drop courses between semesters, with a warning when a prerequisite isn't completed earlier in the plan
- **Automatic plan generation:** one click builds a valid plan, with an adjustable credit limit, and reports anything that can't be placed
- **Accounts:** each student has their own courses and plan

## How it works

**Prerequisites as logic, not text.** A prerequisite like "CS 211 and (MATH 113 or MATH 124)" is stored as an AND/OR tree of course codes next to the original catalog text. The same evaluator powers eligibility checks, roadmap warnings and plan generation.

**Degree requirements as a tree.** Rules like "complete all of these", "choose 5 from this list" and "earn 12 credits from this group" are modeled as nested requirement groups, so real degree rules can be expressed and progress is calculated recursively. A course can count toward a group and its parent at the same time.

**Plan generation as a scheduling problem.** The generator works out which courses a student still needs, then fills semesters one at a time. In each semester it takes courses whose prerequisites were finished in earlier semesters, longest prerequisite chains first, up to the credit limit. Prerequisite cycles are detected, and courses that cannot be placed are reported with a reason.

## Scope

This is a planning tool. It assumes the student already meets all grade, GPA, placement and admission requirements, and it only checks:

- prerequisites (course-to-course logic)
- degree requirement completion
- credit limits per semester

It does not check minimum grades, retake limits, change-of-major rules or permission of instructor.

## Tech stack

| Layer | Technology |
| --- | --- |
| Frontend | React, TypeScript, Vite, TanStack Query, React Router, dnd-kit |
| Backend | Java 21, Spring Boot, Spring Security (JWT) |
| Database | PostgreSQL, Flyway migrations |

```
React app  -->  Spring Boot REST API  -->  PostgreSQL
```

## Data and limitations

- The degree data is entered by hand, because the catalog site does not allow automated access. Only BS Computer Science (2025-2026) is included so far.
- Natural science, Mason Core categories, CS-related electives and free electives are tracked as placeholder slots (for example `MCORE LIT`) rather than specific courses. A student marks a slot completed when they've taken any approved course for it.
- Some courses have prerequisites that haven't been verified, and the planner treats those as having none.
- Every course is assumed to be offered every Fall and Spring. Summer terms and corequisites aren't supported.

## Repository layout

```
backend/    Spring Boot API (controllers, services, planner logic, Flyway migrations)
frontend/   React app
```


## Roadmap

- [x] Course search, requirement tree and progress dashboard
- [x] Roadmap with drag and drop and prerequisite warnings
- [x] Automatic plan generation
- [x] Accounts and login
- [x] Remaining requirement groups, so plans cover the full degree
- [ ] Verified prerequisites and offered terms for every course
- [x] Integration tests and CI
- [ ] More majors

## Authors

- [Marwan Abdelrehim](https://github.com/MarsMayBeHim)

## License

MIT. See [LICENSE](LICENSE).

## Disclaimer

This is an independent student project and is not affiliated with or endorsed by George Mason University. Course and requirement data may be incomplete or out of date. Always confirm your degree plan with your academic advisor.