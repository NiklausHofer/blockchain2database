DELIMITER //
CREATE PROCEDURE block_fee
(IN blk VARCHAR(64)) 
BEGIN
SELECT (in_amount - out_amount) as fee, in_sum.blk_id, in_sum.hash
from
(SELECT block.hash,
  block.blk_id, 
  SUM(output.amount) AS out_amount
FROM block
  inner join (transaction 
    inner join output
    on transaction.tx_id = output.tx_id)
  on transaction.blk_id = block.blk_id
group by block.blk_id, block.hash) as out_sum,
(SELECT block.hash,
  block.blk_id, 
  SUM(input.amount) AS in_amount
FROM block
  inner join (transaction 
    inner join input
    on transaction.tx_id = input.tx_id)
  on transaction.blk_id = block.blk_id
group by block.blk_id, block.hash) as in_sum
where in_sum.blk_id = out_sum.blk_id
and in_sum.hash = blk
group by in_sum.blk_id, in_sum.hash;
END;
//
DELIMITER ;