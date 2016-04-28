-- OUTGOING TRANSACTIONS OF AN ADDRESS AT A GIVEN TIME
DELIMITER //
CREATE PROCEDURE outgoing_addr_tx_time
(IN addr VARCHAR(35), dat DATETIME) 
  BEGIN
    SELECT transaction.tx_id, transaction.tx_hash
    FROM block,transaction INNER JOIN (
          input INNER JOIN output 
          ON input.prev_tx_id = output.tx_id 
          AND input.prev_output_index = output.tx_index)
      ON input.tx_id = transaction.tx_id
    WHERE output.address = addr
    AND block.blk_id = transaction.blk_id
    AND block.time < dat;
  END;
//
DELIMITER ;
