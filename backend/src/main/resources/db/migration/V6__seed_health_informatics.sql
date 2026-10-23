-- BS Health Informatics, catalog year 2025-2026
-- Sources: College of Public Health planning form, cross-checked against a catalog summary
-- Credit split follows the catalog: Mason Core 34, major 73, electives 13
-- Only formal prerequisites have trees, recommended preparation is display text only
-- Courses marked NEEDS_REVIEW have unverified or partial prerequisites
-- Placeholder slots stand in for "any approved course" requirements
-- Mason Core slots reuse the MCORE placeholders created in V4

WITH u AS (SELECT id FROM university WHERE slug = 'gmu')
INSERT INTO course (university_id, subject, number, title, credits_min, credits_max, prereq_text, prereq_tree, parse_status)
SELECT u.id, v.subject, v.number, v.title, v.credits, v.credits, v.prereq_text, v.tree::jsonb, v.status
FROM u, (VALUES
 ('HAP','201','Health Professions Careers',3,NULL,NULL,'NEEDS_REVIEW'),
 ('HAP','202','Medical Terminology',3,NULL,NULL,'NEEDS_REVIEW'),
 ('HAP','301','Health Care Delivery in the United States',3,NULL,NULL,'NEEDS_REVIEW'),
 ('GCH','300','Introduction to Public Health',3,NULL,NULL,'NEEDS_REVIEW'),
 ('HAP','430','Process Improvement in Healthcare Organizations',3,'HAP 301 with a grade of C or better',
   '{"type":"COURSE","code":"HAP 301"}','PARSED'),
 ('HAP','465','Integration of Professional Skills and Issues',3,NULL,NULL,'NEEDS_REVIEW'),
 ('HAP','498','Health Administration Internship',4,NULL,NULL,'NEEDS_REVIEW'),
 ('HI','308','Public Health Informatics',3,NULL,NULL,'NEEDS_REVIEW'),
 ('HI','318','Introduction to IT Methods for Healthcare',3,'Recommended: IT 103, IT 104, or equivalent',NULL,'NEEDS_REVIEW'),
 ('HI','360','Introduction to Health Information Systems',3,NULL,NULL,'NEEDS_REVIEW'),
 ('HI','361','Health Databases',3,'Recommended: HI 360 or equivalent',NULL,'NEEDS_REVIEW'),
 ('HI','436','Electronic Health Data in Process Improvement',3,NULL,NULL,'NEEDS_REVIEW'),
 ('HI','440','Mobile Health',3,NULL,NULL,'NEEDS_REVIEW'),
 ('HI','455','Computer Programming in Health Applications',3,
   'Health IT methods, health databases and EHR analysis courses (HI 318, HI 361, HI 464); verify the exact rule',
   '{"type":"AND","children":[{"type":"COURSE","code":"HI 318"},{"type":"COURSE","code":"HI 361"},{"type":"COURSE","code":"HI 464"}]}','NEEDS_REVIEW'),
 ('HI','456','Health Data Mining and Analysis',3,
   'Health IT methods and database coursework (HI 318, HI 361); SQL knowledge recommended',
   '{"type":"AND","children":[{"type":"COURSE","code":"HI 318"},{"type":"COURSE","code":"HI 361"}]}','NEEDS_REVIEW'),
 ('HI','458','Clinical Informatics Applications in a Health Care Setting',3,'HAP 318 or HI 318; HAP 360 or HI 360',
   '{"type":"AND","children":[{"type":"COURSE","code":"HI 318"},{"type":"COURSE","code":"HI 360"}]}','PARSED'),
 ('HI','459','Health Data Standards and Interoperability',3,
   'Recommended: HAP 301, HAP 361 or HI 361, or instructor permission',NULL,'NEEDS_REVIEW'),
 ('HI','460','Health Information Technology Project Management',3,'Recommended: HI 360',NULL,'NEEDS_REVIEW'),
 ('HI','461','Internet and Web Technology Applications for Healthcare',3,'Recommended: HI 360',NULL,'NEEDS_REVIEW'),
 ('HI','462','Privacy and Security in Health Informatics',3,'Recommended: HI 360',NULL,'NEEDS_REVIEW'),
 ('HI','464','Electronic Health Record Configuration and Data Analysis',3,'Recommended: HI 360',NULL,'NEEDS_REVIEW'),
 ('HI','489','Health Informatics Pre-Internship Seminar',3,'Recommended: HAP 301 and senior standing',NULL,'NEEDS_REVIEW'),
 ('HI','498','Health Informatics Internship',4,NULL,NULL,'NEEDS_REVIEW'),
 ('IT','104','Introduction to Computing',3,NULL,NULL,'NEEDS_REVIEW'),
 ('STAT','250','Introductory Statistics I',3,NULL,NULL,'NEEDS_REVIEW'),
 ('MATH','108','Introductory Calculus with Business Applications',3,NULL,NULL,'NEEDS_REVIEW'),
 ('HINFEL','1','Health Informatics Elective 1 (any approved course)',3,NULL,NULL,'NONE'),
 ('HINFEL','2','Health Informatics Elective 2 (any approved course)',3,NULL,NULL,'NONE'),
 ('HINFEL','3','Health Informatics Elective 3 (any approved course)',3,NULL,NULL,'NONE'),
 ('HINFEL','4','Health Informatics Elective 4 (any approved course, 4 credits)',4,NULL,NULL,'NONE'),
 ('NSLAB','1','Natural Science with lab (BIOL 103 and 105 recommended)',4,NULL,NULL,'NONE'),
 ('NSLAB','2','Natural Science without lab (any approved course)',3,NULL,NULL,'NONE')
) AS v(subject, number, title, credits, prereq_text, tree, status)
ON CONFLICT (university_id, subject, number) DO NOTHING;

UPDATE course
SET title = 'Mason Core: Written Communication (ENGH 100, 101 or 123)'
WHERE subject = 'MCORE' AND number = 'WRIT';

-- Terms taken from the (F) and (S) tags on the planning form
-- HAP 498 is Spring-only on the older 2022-2023 form
INSERT INTO course_offering (course_id, term_type)
SELECT c.id, o.term
FROM (VALUES
 ('HI','318','FALL'),('HI','440','FALL'),('HI','456','FALL'),('HI','458','FALL'),('HI','460','FALL'),
 ('HI','308','SPRING'),('HI','436','SPRING'),('HI','455','SPRING'),('HI','459','SPRING'),
 ('HI','461','SPRING'),('HI','462','SPRING'),('HI','464','SPRING'),('HI','498','SPRING'),
 ('HAP','498','SPRING')
) AS o(subject, number, term)
JOIN course c ON c.subject = o.subject AND c.number = o.number
ON CONFLICT DO NOTHING;

INSERT INTO program (university_id, name, degree_type, catalog_year, total_credits)
SELECT id, 'Health Informatics', 'BS', '2025-2026', 120 FROM university WHERE slug = 'gmu';

INSERT INTO requirement_group (program_id, parent_id, name, rule_type, min_courses, min_credits, sort_order)
SELECT p.id, NULL, g.name, g.rule_type, g.min_courses, NULL, g.sort_order
FROM program p, (VALUES
 ('Health Informatics Core','ALL',NULL,1),
 ('Internship (HAP 498 or HI 498)','CHOOSE_N',1,2),
 ('Health Informatics Electives','ALL',NULL,3),
 ('Quantitative and Technology','ALL',NULL,4),
 ('Mason Core','ALL',NULL,5),
 ('Natural Science','ALL',NULL,6)
) AS g(name, rule_type, min_courses, sort_order)
WHERE p.name = 'Health Informatics' AND p.catalog_year = '2025-2026';

INSERT INTO requirement_group_course (group_id, course_id)
SELECT g.id, c.id
FROM (VALUES
 ('Health Informatics Core','HAP 201'),('Health Informatics Core','HAP 202'),
 ('Health Informatics Core','HAP 301'),('Health Informatics Core','GCH 300'),
 ('Health Informatics Core','HAP 430'),('Health Informatics Core','HAP 465'),
 ('Health Informatics Core','HI 308'),('Health Informatics Core','HI 318'),
 ('Health Informatics Core','HI 360'),('Health Informatics Core','HI 361'),
 ('Health Informatics Core','HI 436'),('Health Informatics Core','HI 440'),
 ('Health Informatics Core','HI 455'),('Health Informatics Core','HI 456'),
 ('Health Informatics Core','HI 458'),('Health Informatics Core','HI 459'),
 ('Health Informatics Core','HI 460'),('Health Informatics Core','HI 461'),
 ('Health Informatics Core','HI 462'),('Health Informatics Core','HI 464'),
 ('Health Informatics Core','HI 489'),
 ('Internship (HAP 498 or HI 498)','HAP 498'),('Internship (HAP 498 or HI 498)','HI 498'),
 ('Health Informatics Electives','HINFEL 1'),('Health Informatics Electives','HINFEL 2'),
 ('Health Informatics Electives','HINFEL 3'),('Health Informatics Electives','HINFEL 4'),
 ('Quantitative and Technology','IT 104'),('Quantitative and Technology','STAT 250'),
 ('Quantitative and Technology','MATH 108'),
 ('Mason Core','MCORE WRIT'),('Mason Core','MCORE ORAL'),('Mason Core','MCORE LIT'),
 ('Mason Core','MCORE ARTS'),('Mason Core','MCORE HIST'),('Mason Core','MCORE SOCIAL'),
 ('Mason Core','MCORE GLOBAL'),('Mason Core','ENGH 302'),
 ('Natural Science','NSLAB 1'),('Natural Science','NSLAB 2')
) AS m(group_name, code)
JOIN program p ON p.name = 'Health Informatics' AND p.catalog_year = '2025-2026'
JOIN requirement_group g ON g.program_id = p.id AND g.name = m.group_name
JOIN course c ON c.subject || ' ' || c.number = m.code;