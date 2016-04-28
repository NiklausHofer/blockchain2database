-- Calculate the sum of all inputs within a given block
DELIMITER //
CREATE PROCEDURE block_input_amount
(IN blk VARCHAR(64)) 
  BEGIN
    SELECT block.hash,
      block.blk_id, 
      SUM(input.amount)
    FROM block
      inner join (transaction 
        inner join input
        ON transaction.tx_id = input.tx_id)
      ON transaction.blk_id = block.blk_id
    WHERE block.hash = blk
    GROUP BY block.blk_id, block.hash;
  END;
//
DELIMITER ;
