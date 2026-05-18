-- удаление пользователя: каскад по операциям, split и кошелькам-владельцу
ALTER TABLE transactions
    DROP CONSTRAINT IF EXISTS transactions_author_id_fkey;
ALTER TABLE transactions
    ADD CONSTRAINT transactions_author_id_fkey
        FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE split_expenses
    DROP CONSTRAINT IF EXISTS split_expenses_user_id_fkey;
ALTER TABLE split_expenses
    ADD CONSTRAINT split_expenses_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE wallets
    DROP CONSTRAINT IF EXISTS wallets_owner_id_fkey;
ALTER TABLE wallets
    ADD CONSTRAINT wallets_owner_id_fkey
        FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE;
