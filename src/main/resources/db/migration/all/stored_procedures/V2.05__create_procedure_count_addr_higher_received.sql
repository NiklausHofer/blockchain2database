-- Count how many addresses currently have a balance higher than the given parameter
DELIMITER // 
CREATE PROCEDURE count_addr_which_received_higher_than( myamount bigint )
  BEGIN
    SELECT COUNT(address) AS count
    FROM
      (
        SELECT address,SUM(amount) AS sum_amount
        FROM output
        GROUP BY address
      ) AS sum_addr
      WHERE sum_amount > myamount;
  END;
//
DELIMITER ;


