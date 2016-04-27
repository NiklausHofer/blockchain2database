-- Calcuate a given transaction's fee
DELIMITER //
CREATE PROCEDURE tx_fee
(IN tx VARCHAR(64)) 
  BEGIN
    SELECT (in_amount - out_amount) AS fee, in_sum.tx_id, in_sum.tx_hash
    FROM
    (SELECT transaction.tx_hash,
      transaction.tx_id, 
      SUM(output.amount) AS out_amount
      FROM
      transaction 
        inner join output
        ON transaction.tx_id = output.tx_id
    GROUP BY transaction.tx_id, transaction.tx_hash) AS out_sum,
    (SELECT transaction.tx_hash,
      transaction.tx_id, 
      SUM(input.amount) AS in_amount
    FROM transaction 
        inner join input
        ON transaction.tx_id = input.tx_id
    GROUP BY transaction.tx_id, transaction.tx_hash) AS in_sum
    WHERE in_sum.tx_id = out_sum.tx_id
    AND in_sum.tx_hash = tx
    GROUP BY in_sum.tx_id, in_sum.tx_hash;
  END;
//
DELIMITER ;
