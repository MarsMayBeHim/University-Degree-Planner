INSERT INTO app_user (email, password_hash, university_id, program_id, start_year, start_term)
SELECT 'test@example.com', 'not-a-real-hash', u.id, p.id, 2025, 'FALL'
FROM university u, program p
WHERE u.slug = 'gmu' AND p.name = 'Computer Science' AND p.catalog_year = '2025-2026';