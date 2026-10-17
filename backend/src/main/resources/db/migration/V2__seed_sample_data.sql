INSERT INTO course (university_id, subject, number, title, credits_min, credits_max, prereq_text, prereq_tree, parse_status) VALUES
((SELECT id FROM university WHERE slug = 'gmu'), 'CS', '110', 'Essentials of Computer Science', 3, 3, NULL, NULL, 'NONE'),
((SELECT id FROM university WHERE slug = 'gmu'), 'CS', '112', 'Introduction to Computer Programming', 4, 4, NULL, NULL, 'NONE'),
((SELECT id FROM university WHERE slug = 'gmu'), 'CS', '211', 'Object-Oriented Programming', 3, 3, 'CS 112', '{"type":"COURSE","code":"CS 112"}', 'PARSED'),
((SELECT id FROM university WHERE slug = 'gmu'), 'MATH', '113', 'Analytic Geometry and Calculus I', 4, 4, NULL, NULL, 'NONE'),
((SELECT id FROM university WHERE slug = 'gmu'), 'MATH', '114', 'Analytic Geometry and Calculus II', 4, 4, 'MATH 113', '{"type":"COURSE","code":"MATH 113"}', 'PARSED');

INSERT INTO program (university_id, name, degree_type, catalog_year, total_credits) VALUES
((SELECT id FROM university WHERE slug = 'gmu'), 'Computer Science', 'BS', '2025-2026', 120);

INSERT INTO requirement_group (program_id, parent_id, name, rule_type, sort_order) VALUES
((SELECT id FROM program WHERE name = 'Computer Science'), NULL, 'Major Core', 'ALL', 1);

INSERT INTO requirement_group_course (group_id, course_id)
SELECT g.id, c.id
FROM requirement_group g, course c
WHERE g.name = 'Major Core' AND c.subject = 'CS' AND c.number IN ('110', '112' , '211');