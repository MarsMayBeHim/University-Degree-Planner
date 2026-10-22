-- Prerequisites from the pasted catalog notes (not independently verified against the 2025-2026 archive)
-- prereq_text keeps the full catalog wording for display
-- prereq_tree only references courses and placeholder slots that exist in this degree plan
-- Grades, placement scores and honors alternatives are ignored by design

UPDATE course c
SET prereq_text = v.prereq_text, prereq_tree = v.tree::jsonb, parse_status = v.status
FROM (VALUES
 ('CS','110',NULL,NULL,'NONE'),
 ('CS','112','Math placement requirement may apply.',NULL,'NONE'),
 ('CS','367','CS 262 or CS 222 or ECE 340; MATH 125; CS 110',
   '{"type":"AND","children":[{"type":"COURSE","code":"CS 262"},{"type":"COURSE","code":"MATH 125"},{"type":"COURSE","code":"CS 110"}]}','PARSED'),
 ('CS','306','CS 105 or CS 110; COMM 100/101 and ENGH 302, or HNRS 260/261',
   '{"type":"AND","children":[{"type":"COURSE","code":"CS 110"},{"type":"COURSE","code":"MCORE ORAL"},{"type":"COURSE","code":"ENGH 302"}]}','PARSED'),
 ('CS','405','CS 105 or CS 110; COMM 100/101 and ENGH 302, or HNRS 260/261',
   '{"type":"AND","children":[{"type":"COURSE","code":"CS 110"},{"type":"COURSE","code":"MCORE ORAL"},{"type":"COURSE","code":"ENGH 302"}]}','PARSED'),
 ('CS','471','CS 310 or ECE 340; CS 367 or ECE 445',
   '{"type":"AND","children":[{"type":"COURSE","code":"CS 310"},{"type":"COURSE","code":"CS 367"}]}','PARSED'),
 ('CS','452','CS 325 or CS 351 or CS 451',
   '{"type":"COURSE","code":"CS 451"}','PARSED'),
 ('CS','455','CS 310; CS 367; STAT 344 or MATH 352',
   '{"type":"AND","children":[{"type":"COURSE","code":"CS 310"},{"type":"COURSE","code":"CS 367"},{"type":"COURSE","code":"STAT 344"}]}','PARSED'),
 ('CS','463','CS 330; CS 367; CS 310',
   '{"type":"AND","children":[{"type":"COURSE","code":"CS 330"},{"type":"COURSE","code":"CS 367"},{"type":"COURSE","code":"CS 310"}]}','PARSED'),
 ('CS','468','CS 310 and CS 367',
   '{"type":"AND","children":[{"type":"COURSE","code":"CS 310"},{"type":"COURSE","code":"CS 367"}]}','PARSED'),
 ('CS','475','CS 310 and CS 367',
   '{"type":"AND","children":[{"type":"COURSE","code":"CS 310"},{"type":"COURSE","code":"CS 367"}]}','PARSED'),
 ('CS','477','CS 310 and CS 367',
   '{"type":"AND","children":[{"type":"COURSE","code":"CS 310"},{"type":"COURSE","code":"CS 367"}]}','PARSED'),
 ('CS','485','CS 262, CS 310 and MATH 203',
   '{"type":"AND","children":[{"type":"COURSE","code":"CS 262"},{"type":"COURSE","code":"CS 310"},{"type":"COURSE","code":"MATH 203"}]}','PARSED'),
 ('CS','489','MATH 203 and CS 480 or CS 484; MATH 213 and STAT 344 recommended',
   '{"type":"AND","children":[{"type":"COURSE","code":"MATH 203"},{"type":"OR","children":[{"type":"COURSE","code":"CS 480"},{"type":"COURSE","code":"CS 484"}]}]}','PARSED'),
 ('MATH','113','MATH 105 or MATH 104, or a qualifying placement score',NULL,'NONE'),
 ('MATH','114','MATH 113; or MATH 123 and MATH 124; or MATH 115',
   '{"type":"COURSE","code":"MATH 113"}','PARSED'),
 ('MATH','125',NULL,NULL,'NONE'),
 ('MATH','203','MATH 114 or MATH 116',
   '{"type":"COURSE","code":"MATH 114"}','PARSED'),
 ('MATH','213','MATH 114 or MATH 116',
   '{"type":"COURSE","code":"MATH 114"}','PARSED'),
 ('STAT','344','MATH 114 or MATH 116',
   '{"type":"COURSE","code":"MATH 114"}','PARSED'),
 ('ENGH','302','One approved lower-level composition course and one approved literature course',
   '{"type":"AND","children":[{"type":"COURSE","code":"MCORE WRIT"},{"type":"COURSE","code":"MCORE LIT"}]}','PARSED')
) AS v(subject, number, prereq_text, tree, status)
WHERE c.subject = v.subject AND c.number = v.number;

-- These already have trees from V2, and their caveats are outside the planner scope
UPDATE course
SET parse_status = 'PARSED'
WHERE subject = 'CS' AND number IN ('211', '262', '321') AND prereq_tree IS NOT NULL;