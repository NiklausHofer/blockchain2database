-- CALCULATE THE COINDAYS THAT WHERE DESTROYED BY A TRANSACTION
-- GIVEN THE TRANSACTION ID
--
-- CALCULATE FOR EACH OUTPUT WICH WAS SPENT BY THIS TRANSACTION
-- THE TIME DIFFERENCE OF THE TRANSACTION CONTAINING THE OUTPUT
-- AND THE SPENDING (THE GIVEN) TRANSACTION
-- MULTIPLIED BY THE OUTPUTS AMOUNT
-- THEN SUM UP ALL DESTROYED DAYS

DELIMITER //
CREATE PROCEDURE tx_btcdays_destroyed
(IN TX_X BIGINT)
  BEGIN

    SELECT sum(btc_days) AS btc_days_destroyed,
      spent_by_tx
    FROM (
    SELECT
      (
        (
           TIMESTAMPDIFF(SECOND,transaction.blk_time,output.spent_at) / 86400
        ) *
        (output.amount/100000000)
      )
      AS btc_days,
      output.spent_by_tx
    FROM transaction
     INNER JOIN output
     ON output.tx_id = transaction.tx_id
    WHERE output.spent_at IS NOT NULL
    AND spent_by_tx = TX_X
    ) AS days_per_output
    GROUP BY spent_by_tx;

  END;
//
DELIMITER ;
