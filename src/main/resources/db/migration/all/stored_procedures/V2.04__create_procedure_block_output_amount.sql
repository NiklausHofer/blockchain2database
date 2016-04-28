-- Calculate the sum of all output amounts within a given block
DELIMITER //
CREATE PROCEDURE block_output_amount
(IN blk VARCHAR(64)) 
  BEGIN
    SELECT block.hash,
      block.blk_id, 
      SUM(output.amount)
    FROM block
      inner join (transaction 
        inner join output
        ON transaction.tx_id = output.tx_id)
      ON transaction.blk_id = block.blk_id
    WHERE block.hash = blk
    GROUP BY block.blk_id, block.hash;
  END;
//
DELIMITER ;
