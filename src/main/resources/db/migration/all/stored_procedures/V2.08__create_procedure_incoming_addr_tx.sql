-- INCOMMING TRANSACTIONS OF AN ADDRESS
DELIMITER //
CREATE PROCEDURE incomming_addr_tx
(IN addr VARCHAR(35)) 
  BEGIN
    SELECT transaction.tx_id, transaction.tx_hash
    FROM transaction, output
    WHERE output.address = addr
    AND output.tx_id = transaction.tx_id;
  END;
//
DELIMITER ;
