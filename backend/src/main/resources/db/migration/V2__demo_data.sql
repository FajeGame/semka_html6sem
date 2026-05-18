-- пароль для papa и mama: password
INSERT INTO users (id, email, nick, password_hash, role) VALUES
(1, 'papa@test.ru', 'papa', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER'),
(2, 'mama@test.ru', 'mama', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER');

INSERT INTO wallets (id, name, owner_id) VALUES (1, 'Семья', 1);

INSERT INTO wallet_members (wallet_id, user_id, member_role, can_see_budget) VALUES
(1, 1, 'WALLET_OWNER', TRUE),
(1, 2, 'WALLET_MEMBER', TRUE);

INSERT INTO categories (id, wallet_id, name, tip, icon_key, color_bg, created_by) VALUES
(1, 1, 'Продукты', 'EXPENSE', 'cart', '#d8f3e4', 1),
(2, 1, 'Транспорт', 'EXPENSE', 'car', '#cce8f8', 1),
(3, 1, 'Дом', 'EXPENSE', 'home', '#f5e6d3', 1),
(4, 1, 'Зарплата', 'INCOME', 'wallet', '#AAF0D1', 1),
(5, 1, 'Подарки', 'INCOME', 'gift', '#d1c4e9', 1);

INSERT INTO transactions (wallet_id, author_id, category_id, type, amount, transaction_date, comment) VALUES
(1, 1, 4, 'INCOME', 120000, CURRENT_DATE - INTERVAL '5 days', 'зарплата'),
(1, 2, 1, 'EXPENSE', 3500, CURRENT_DATE - INTERVAL '3 days', 'магазин');

INSERT INTO budgets (wallet_id, category_id, period_start, period_end, limit_amount) VALUES
(1, NULL, DATE_TRUNC('month', CURRENT_DATE)::DATE,
 (DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month' - INTERVAL '1 day')::DATE, 50000),
(1, 1, DATE_TRUNC('month', CURRENT_DATE)::DATE,
 (DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month' - INTERVAL '1 day')::DATE, 15000);

SELECT setval('users_id_seq', 2);
SELECT setval('wallets_id_seq', 1);
SELECT setval('categories_id_seq', 5);
SELECT setval('transactions_id_seq', 2);
