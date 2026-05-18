-- владелец всегда должен быть в wallet_members
INSERT INTO wallet_members (wallet_id, user_id, member_role, can_see_budget)
SELECT w.id, w.owner_id, 'WALLET_OWNER', TRUE
FROM wallets w
WHERE NOT EXISTS (
    SELECT 1 FROM wallet_members m
    WHERE m.wallet_id = w.id AND m.user_id = w.owner_id
);

-- участники без существующего кошелька
DELETE FROM wallet_members wm
WHERE NOT EXISTS (SELECT 1 FROM wallets w WHERE w.id = wm.wallet_id);

-- пустые кошельки без категорий и операций (мусор от тестов)
DELETE FROM wallets w
WHERE NOT EXISTS (SELECT 1 FROM categories c WHERE c.wallet_id = w.id)
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.wallet_id = w.id);

-- у владельца несколько пустых кошельков (без операций) — оставляем самый старый
DELETE FROM wallets w
WHERE NOT EXISTS (SELECT 1 FROM transactions t WHERE t.wallet_id = w.id)
  AND EXISTS (
    SELECT 1 FROM wallets w2
    WHERE w2.owner_id = w.owner_id
      AND w2.id < w.id
      AND NOT EXISTS (SELECT 1 FROM transactions t2 WHERE t2.wallet_id = w2.id)
  );
