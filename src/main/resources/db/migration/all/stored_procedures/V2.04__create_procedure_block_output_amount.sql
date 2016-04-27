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
    on transaction.tx_id = output.tx_id)
  on transaction.blk_id = block.blk_id
where block.hash = blk
group by block.blk_id, block.hash;
END;
//
DELIMITER ;