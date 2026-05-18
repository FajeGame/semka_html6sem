-- bcrypt для пароля "password" (совместим с Spring BCryptPasswordEncoder)
UPDATE users
SET password_hash = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
WHERE email IN ('papa@test.ru', 'mama@test.ru');
