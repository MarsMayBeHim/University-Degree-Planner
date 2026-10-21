-- Placeholder slots for requirements that list "approved" courses, so plans cover all 120 credits
-- A student marks a slot completed when they took any approved course for it

WITH u AS (SELECT id FROM university WHERE slug = 'gmu')
INSERT INTO course (university_id, subject, number, title, credits_min, credits_max, prereq_text, prereq_tree, parse_status)
SELECT u.id, v.subject, v.number, v.title, v.credits, v.credits, v.prereq_text, v.tree::jsonb, v.status
FROM u, (VALUES
 ('MCORE','ORAL','Mason Core: Oral Communication (COMM 100 or 101)',3,NULL,NULL,'NONE'),
 ('MCORE','WRIT','Mason Core: Written Communication (ENGH 100 or 101)',3,NULL,NULL,'NONE'),
 ('MCORE','LIT','Mason Core: Literature (any approved course)',3,NULL,NULL,'NONE'),
 ('MCORE','ARTS','Mason Core: Arts (any approved course)',3,NULL,NULL,'NONE'),
 ('MCORE','HIST','Mason Core: Global History (any approved course)',3,NULL,NULL,'NONE'),
 ('MCORE','SOCIAL','Mason Core: Social and Behavioral Sciences (any approved course)',3,NULL,NULL,'NONE'),
 ('MCORE','GLOBAL','Mason Core: Global Contexts (any approved course)',3,NULL,NULL,'NONE'),
 ('NSCI','1','Natural Science: first sequence course with lab',4,NULL,NULL,'NONE'),
 ('NSCI','2','Natural Science: second sequence course with lab',4,'First course in the same sequence','{"type":"COURSE","code":"NSCI 1"}','PARSED'),
 ('NSCI','3','Natural Science: one more approved course (4 credits total)',4,NULL,NULL,'NONE'),
 ('CSREL','1','CS-Related Elective 1 (any approved course)',3,NULL,NULL,'NONE'),
 ('CSREL','2','CS-Related Elective 2 (any approved course)',3,NULL,NULL,'NONE'),
 ('FREE','1','Free Elective 1 (any course, 4 credits)',4,NULL,NULL,'NONE'),
 ('FREE','2','Free Elective 2 (any course, 4 credits)',4,NULL,NULL,'NONE')
) AS v(subject, number, title, credits, prereq_text, tree, status);

-- Standard calculus sequence, please verify against the catalog
UPDATE course
SET prereq_text = 'MATH 113', prereq_tree = '{"type":"COURSE","code":"MATH 113"}', parse_status = 'PARSED'
WHERE subject = 'MATH' AND number = '114';

UPDATE course
SET prereq_text = 'MATH 114', prereq_tree = '{"type":"COURSE","code":"MATH 114"}', parse_status = 'PARSED'
WHERE subject = 'MATH' AND number = '213';

INSERT INTO requirement_group (program_id, parent_id, name, rule_type, min_courses, min_credits, sort_order)
SELECT p.id, NULL, g.name, 'ALL', NULL, NULL, g.sort_order
FROM program p, (VALUES
 ('Mason Core: Other Categories', 6),
 ('Natural Science', 7),
 ('CS-Related Electives', 8),
 ('Free Electives', 9)
) AS g(name, sort_order)
WHERE p.name = 'Computer Science' AND p.catalog_year = '2025-2026';

INSERT INTO requirement_group_course (group_id, course_id)
SELECT g.id, c.id
FROM (VALUES
 ('Mason Core: Other Categories','MCORE ORAL'),('Mason Core: Other Categories','MCORE WRIT'),
 ('Mason Core: Other Categories','MCORE LIT'),('Mason Core: Other Categories','MCORE ARTS'),
 ('Mason Core: Other Categories','MCORE HIST'),('Mason Core: Other Categories','MCORE SOCIAL'),
 ('Mason Core: Other Categories','MCORE GLOBAL'),
 ('Natural Science','NSCI 1'),('Natural Science','NSCI 2'),('Natural Science','NSCI 3'),
 ('CS-Related Electives','CSREL 1'),('CS-Related Electives','CSREL 2'),
 ('Free Electives','FREE 1'),('Free Electives','FREE 2')
) AS m(group_name, code)
JOIN requirement_group g ON g.name = m.group_name
JOIN course c ON c.subject || ' ' || c.number = m.code;