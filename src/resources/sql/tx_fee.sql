
DELIMITER //
CREATE PROCEDURE tx_fee
(IN tx VARCHAR(64)) 
BEGIN
SELECT (in_amount - out_amount) as fee, in_sum.tx_id, in_sum.tx_hash
from
(SELECT transaction.tx_hash,
  transaction.tx_id, 
  SUM(output.amount) AS out_amount
  FROM
  transaction 
    inner join output
    on transaction.tx_id = output.tx_id
group by transaction.tx_id, transaction.tx_hash) as out_sum,
(SELECT transaction.tx_hash,
  transaction.tx_id, 
  SUM(input.amount) AS in_amount
FROM transaction 
    inner join input
    on transaction.tx_id = input.tx_id
group by transaction.tx_id, transaction.tx_hash) as in_sum
where in_sum.tx_id = out_sum.tx_id
and in_sum.tx_hash = tx
group by in_sum.tx_id, in_sum.tx_hash;
END;
//
DELIMITER ;