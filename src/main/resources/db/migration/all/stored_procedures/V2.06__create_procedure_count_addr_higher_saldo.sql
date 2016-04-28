-- Count how many addresses currently have a balance higher than the given parameter
DELIMITER // 
CREATE PROCEDURE count_addr_with_balance_higher_than( myamount bigint )
  BEGIN
    SELECT COUNT(address) AS count
    FROM
      (
        SELECT address,SUM(amount) AS sum_amount
        FROM output
          WHERE spent_at is NULL
          GROUP BY address
      ) AS sum_addr
      WHERE sum_amount > myamount;
  END;
//
DELIMITER ;


