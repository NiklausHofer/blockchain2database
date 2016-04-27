-- Get the balance of an address at a given time
DELIMITER //
CREATE PROCEDURE get_balance( addr varchar(255), time DATETIME )
  BEGIN
    SELECT SUM(amount) as balance
      FROM output
      LEFT JOIN transaction
        ON output.tx_id = transaction.tx_id
      WHERE transaction.blk_time < time
        AND (
          output.spent_at is NULL
          OR output.spent_at > time
        )  
        AND address = addr;
  END;
//
DELIMITER ;
