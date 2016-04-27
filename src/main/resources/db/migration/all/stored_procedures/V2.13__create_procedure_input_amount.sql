-- Calculate total input value of a given transaction
DELIMITER //
CREATE PROCEDURE tx_input_amount
(IN tx VARCHAR(64)) 
  BEGIN
    SELECT transaction.tx_hash,
      transaction.tx_id, 
      SUM(output.amount)
    FROM transaction 
        inner join output
        ON transaction.tx_id = output.tx_id
    WHERE transaction.tx_hash = tx
    GROUP BY transaction.tx_id, transaction.tx_hash;
  END;
//
DELIMITER ;
