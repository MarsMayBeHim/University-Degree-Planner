-- Seed: GMU BS Computer Science, catalog year 2025-2026 (partial, see notes in chat)
-- parse_status NEEDS_REVIEW means prerequisites are unknown or have caveats, not "no prerequisites"
-- Elective credits are assumed to be 3 each (15 credits for 5 senior electives)

WITH u AS (SELECT id FROM university WHERE slug = 'gmu')
INSERT INTO course (university_id, subject, number, title, credits_min, credits_max, prereq_text, prereq_tree, parse_status)
SELECT u.id, v.subject, v.number, v.title, v.credits, v.credits, v.prereq_text, v.tree::jsonb, v.status
FROM u, (VALUES
 -- CS core
 ('CS','110','Essentials of Computer Science',3,NULL,NULL,'NONE'),
 ('CS','112','Introduction to Computer Programming',4,'Math placement requirement may apply.',NULL,'NONE'),
 ('CS','211','Object-Oriented Programming',3,'CS 110 and CS 112/CS 109, subject to the applicable course rules.','{"type":"AND","children":[{"type":"COURSE","code":"CS 110"},{"type":"OR","children":[{"type":"COURSE","code":"CS 112"},{"type":"COURSE","code":"CS 109"}]}]}','PARSED'),
 ('CS','262','Introduction to Low-Level Programming',3,'CS 110 (may be concurrent) and CS 211, or approved alternatives.','{"type":"AND","children":[{"type":"COURSE","code":"CS 110"},{"type":"COURSE","code":"CS 211"}]}','PARSED'),
 ('CS','310','Data Structures',3,'CS 211, MATH 125, and MATH 113, 124, or 115.','{"type":"AND","children":[{"type":"COURSE","code":"CS 211"},{"type":"COURSE","code":"MATH 125"},{"type":"OR","children":[{"type":"COURSE","code":"MATH 113"},{"type":"COURSE","code":"MATH 124"},{"type":"COURSE","code":"MATH 115"}]}]}','PARSED'),
 ('CS','321','Software Engineering',3,'CS 310 and ENGH 302 or an approved honors writing alternative.','{"type":"AND","children":[{"type":"COURSE","code":"CS 310"},{"type":"COURSE","code":"ENGH 302"}]}','PARSED'),
 ('CS','330','Formal Methods and Models',3,'CS 211 and MATH 125.','{"type":"AND","children":[{"type":"COURSE","code":"CS 211"},{"type":"COURSE","code":"MATH 125"}]}','PARSED'),
 ('CS','367','Computer Systems and Programming',4,NULL,NULL,'NEEDS_REVIEW'),
 ('CS','306','Synthesis of Ethics and Law',3,NULL,NULL,'NEEDS_REVIEW'),
 ('CS','405','Ethics and Law in Computing',3,NULL,NULL,'NEEDS_REVIEW'),
 ('CS','471','Operating Systems',3,NULL,NULL,'NEEDS_REVIEW'),
 ('CS','483','Analysis of Algorithms',3,'CS 310, CS 330, and MATH 125.','{"type":"AND","children":[{"type":"COURSE","code":"CS 310"},{"type":"COURSE","code":"CS 330"},{"type":"COURSE","code":"MATH 125"}]}','PARSED'),
 -- Math, statistics, writing
 ('MATH','113','Analytic Geometry and Calculus I',4,NULL,NULL,'NEEDS_REVIEW'),
 ('MATH','114','Analytic Geometry and Calculus II',4,NULL,NULL,'NEEDS_REVIEW'),
 ('MATH','125','Discrete Mathematics I',3,NULL,NULL,'NEEDS_REVIEW'),
 ('MATH','203','Linear Algebra',3,NULL,NULL,'NEEDS_REVIEW'),
 ('MATH','213','Calculus III',3,NULL,NULL,'NEEDS_REVIEW'),
 ('STAT','344','Probability and Statistics for Engineers and Scientists I',3,NULL,NULL,'NEEDS_REVIEW'),
 ('ENGH','302','Advanced Composition',3,NULL,NULL,'NEEDS_REVIEW'),
 -- Senior CS electives
 ('CS','425','Game Programming I',3,'CS 310 and CS 351','{"type":"AND","children":[{"type":"COURSE","code":"CS 310"},{"type":"COURSE","code":"CS 351"}]}','NEEDS_REVIEW'),
 ('CS','440','Compilers',3,'CS 310, CS 330, CS 367','{"type":"AND","children":[{"type":"COURSE","code":"CS 310"},{"type":"COURSE","code":"CS 330"},{"type":"COURSE","code":"CS 367"}]}','PARSED'),
 ('CS','450','Database Concepts',3,'CS 310 and CS 330','{"type":"AND","children":[{"type":"COURSE","code":"CS 310"},{"type":"COURSE","code":"CS 330"}]}','PARSED'),
 ('CS','451','Computer Graphics',3,'MATH 203, CS 310, CS 367','{"type":"AND","children":[{"type":"COURSE","code":"MATH 203"},{"type":"COURSE","code":"CS 310"},{"type":"COURSE","code":"CS 367"}]}','PARSED'),
 ('CS','452','Virtual Reality',3,NULL,NULL,'NEEDS_REVIEW'),
 ('CS','453','Computational Photography',3,'MATH 203, CS 310, CS 330','{"type":"AND","children":[{"type":"COURSE","code":"MATH 203"},{"type":"COURSE","code":"CS 310"},{"type":"COURSE","code":"CS 330"}]}','PARSED'),
 ('CS','455','Computer Communications and Networking',3,NULL,NULL,'NEEDS_REVIEW'),
 ('CS','463','Comparative Programming Languages',3,NULL,NULL,'NEEDS_REVIEW'),
 ('CS','465','Computer Systems Architecture',3,'CS 367','{"type":"COURSE","code":"CS 367"}','PARSED'),
 ('CS','468','Secure Programming and Systems',3,NULL,NULL,'NEEDS_REVIEW'),
 ('CS','469','Security Engineering',3,NULL,NULL,'NEEDS_REVIEW'),
 ('CS','475','Concurrent and Distributed Systems',3,NULL,NULL,'NEEDS_REVIEW'),
 ('CS','477','Mobile Application Development',3,NULL,NULL,'NEEDS_REVIEW'),
 ('CS','478','Natural Language Processing',3,'CS 310 and CS 330','{"type":"AND","children":[{"type":"COURSE","code":"CS 310"},{"type":"COURSE","code":"CS 330"}]}','PARSED'),
 ('CS','480','Introduction to Artificial Intelligence',3,'CS 310 and CS 330','{"type":"AND","children":[{"type":"COURSE","code":"CS 310"},{"type":"COURSE","code":"CS 330"}]}','PARSED'),
 ('CS','482','Computer Vision',3,'CS 310, MATH 203, STAT 344','{"type":"AND","children":[{"type":"COURSE","code":"CS 310"},{"type":"COURSE","code":"MATH 203"},{"type":"COURSE","code":"STAT 344"}]}','PARSED'),
 ('CS','484','Data Mining',3,'CS 262, CS 310, MATH 203','{"type":"AND","children":[{"type":"COURSE","code":"CS 262"},{"type":"COURSE","code":"CS 310"},{"type":"COURSE","code":"MATH 203"}]}','PARSED'),
 ('CS','485','Autonomous Robotics',3,NULL,NULL,'NEEDS_REVIEW'),
 ('CS','487','Introduction to Cryptography',3,'CS 310, CS 330, STAT 344','{"type":"AND","children":[{"type":"COURSE","code":"CS 310"},{"type":"COURSE","code":"CS 330"},{"type":"COURSE","code":"STAT 344"}]}','PARSED'),
 ('CS','489','Deep Learning',3,'CS 321; CS 367 and CS 483 are recommended corequisites','{"type":"COURSE","code":"CS 321"}','PARSED')
) AS v(subject, number, title, credits, prereq_text, tree, status);

INSERT INTO program (university_id, name, degree_type, catalog_year, total_credits)
SELECT id, 'Computer Science', 'BS', '2025-2026', 120 FROM university WHERE slug = 'gmu';

-- Top-level requirement groups
INSERT INTO requirement_group (program_id, parent_id, name, rule_type, min_courses, min_credits, sort_order)
SELECT p.id, NULL, g.name, g.rule_type, g.min_courses, NULL, g.sort_order
FROM program p, (VALUES
 ('Computer Science Core','ALL',NULL,1),
 ('Senior CS Electives','CHOOSE_N',5,2),
 ('Mathematics','ALL',NULL,3),
 ('Statistics','ALL',NULL,4),
 ('Mason Core: Advanced Composition','ALL',NULL,5)
) AS g(name, rule_type, min_courses, sort_order)
WHERE p.name = 'Computer Science' AND p.catalog_year = '2025-2026';

-- Child groups
INSERT INTO requirement_group (program_id, parent_id, name, rule_type, min_courses, min_credits, sort_order)
SELECT parent.program_id, parent.id, c.name, 'CHOOSE_N', c.min_courses, NULL, c.sort_order
FROM (VALUES
 ('Computer Science Core','Ethics and Law (CS 405 or CS 306)',1,1),
 ('Senior CS Electives','Senior Elective Group A (at least one)',1,1)
) AS c(parent_name, name, min_courses, sort_order)
JOIN requirement_group parent ON parent.name = c.parent_name;

-- Which courses can satisfy which group
INSERT INTO requirement_group_course (group_id, course_id)
SELECT g.id, c.id
FROM (VALUES
 ('Computer Science Core','CS 110'),('Computer Science Core','CS 112'),('Computer Science Core','CS 211'),
 ('Computer Science Core','CS 262'),('Computer Science Core','CS 310'),('Computer Science Core','CS 321'),
 ('Computer Science Core','CS 330'),('Computer Science Core','CS 367'),('Computer Science Core','CS 471'),
 ('Computer Science Core','CS 483'),
 ('Ethics and Law (CS 405 or CS 306)','CS 405'),('Ethics and Law (CS 405 or CS 306)','CS 306'),
 ('Mathematics','MATH 113'),('Mathematics','MATH 114'),('Mathematics','MATH 125'),
 ('Mathematics','MATH 203'),('Mathematics','MATH 213'),
 ('Statistics','STAT 344'),
 ('Mason Core: Advanced Composition','ENGH 302'),
 ('Senior Elective Group A (at least one)','CS 455'),('Senior Elective Group A (at least one)','CS 468'),
 ('Senior Elective Group A (at least one)','CS 475'),('Senior Elective Group A (at least one)','CS 487'),
 ('Senior CS Electives','CS 425'),('Senior CS Electives','CS 440'),('Senior CS Electives','CS 450'),
 ('Senior CS Electives','CS 451'),('Senior CS Electives','CS 452'),('Senior CS Electives','CS 453'),
 ('Senior CS Electives','CS 455'),('Senior CS Electives','CS 463'),('Senior CS Electives','CS 465'),
 ('Senior CS Electives','CS 468'),('Senior CS Electives','CS 469'),('Senior CS Electives','CS 475'),
 ('Senior CS Electives','CS 477'),('Senior CS Electives','CS 478'),('Senior CS Electives','CS 480'),
 ('Senior CS Electives','CS 482'),('Senior CS Electives','CS 484'),('Senior CS Electives','CS 485'),
 ('Senior CS Electives','CS 487'),('Senior CS Electives','CS 489')
) AS m(group_name, code)
JOIN requirement_group g ON g.name = m.group_name
JOIN course c ON c.subject || ' ' || c.number = m.code;