-- Calculate the total of transaction fees within a given block
DELIMITER //
CREATE PROCEDURE block_fee
(IN blk VARCHAR(64)) 
  BEGIN
    SELECT (in_amount - out_amount) AS fee, in_sum.blk_id, in_sum.hash
    FROM
    (SELECT block.hash,
      block.blk_id, 
      SUM(output.amount) AS out_amount
    FROM block
      inner join (transaction 
        inner join output
        ON transaction.tx_id = output.tx_id)
      ON transaction.blk_id = block.blk_id
    GROUP BY block.blk_id, block.hash) AS out_sum,
    (SELECT block.hash,
      block.blk_id, 
      SUM(input.amount) AS in_amount
    FROM block
      inner join (transaction 
        inner join input
        ON transaction.tx_id = input.tx_id)
      ON transaction.blk_id = block.blk_id
    GROUP BY block.blk_id, block.hash) AS in_sum
    WHERE in_sum.blk_id = out_sum.blk_id
    AND in_sum.hash = blk
    GROUP BY in_sum.blk_id, in_sum.hash;
  END;
//
DELIMITER ;
