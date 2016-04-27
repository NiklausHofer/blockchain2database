-- INCOMING TRANSACTIONS OF AN ADDRESS AT A GIVEN TIME

DELIMITER //
CREATE PROCEDURE incomming_addr_tx_time
(IN addr VARCHAR(35), dat DATETIME) 
BEGIN
  SELECT transaction.tx_id, transaction.tx_hash
  FROM transaction, output, block
  WHERE output.address = addr
  AND output.tx_id = transaction.tx_id
  AND block.blk_id = transaction.blk_id
  AND block.time = dat;
END;
//
DELIMITER ;