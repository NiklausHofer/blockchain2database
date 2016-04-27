
DELIMITER //
CREATE PROCEDURE tx_input_amount
(IN tx VARCHAR(64)) 
BEGIN
SELECT transaction.tx_hash,
  transaction.tx_id, 
  SUM(output.amount)
FROM transaction 
    inner join output
    on transaction.tx_id = output.tx_id
where transaction.tx_hash = tx
group by transaction.tx_id, transaction.tx_hash;
END;
//
DELIMITER ;