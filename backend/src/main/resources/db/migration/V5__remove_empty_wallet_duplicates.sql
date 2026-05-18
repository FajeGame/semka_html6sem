-- у владельца есть кошелёк с операциями — удаляем все его пустые дубли
DELETE FROM wallets w
WHERE NOT EXISTS (SELECT 1 FROM transactions t WHERE t.wallet_id = w.id)
  AND EXISTS (
    SELECT 1 FROM wallets w2
    WHERE w2.owner_id = w.owner_id
      AND EXISTS (SELECT 1 FROM transactions t2 WHERE t2.wallet_id = w2.id)
  );
