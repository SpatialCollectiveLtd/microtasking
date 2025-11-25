USE bgprxgmy_microtask;

-- Check if user already exists
SELECT * FROM user WHERE email = 'tech@spatialcollective.com';

-- Insert admin user if not exists
INSERT INTO user (email, name, picture, role, created_at, updated_at)
SELECT * FROM (SELECT 
    'tech@spatialcollective.com' as email,
    'Tech' as name,
    'https://ui-avatars.com/api/?name=Tech&background=0D8ABC&color=fff' as picture,
    'ADMIN' as role,
    NOW() as created_at,
    NOW() as updated_at
) AS tmp
WHERE NOT EXISTS (
    SELECT email FROM user WHERE email = 'tech@spatialcollective.com'
) LIMIT 1;

-- Verify insertion
SELECT * FROM user WHERE email = 'tech@spatialcollective.com';
