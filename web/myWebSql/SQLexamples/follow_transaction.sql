DELIMITER //

CREATE OR REPLACE PROCEDURE depend_on_tx( tx VARCHAR(255) )
BEGIN
  DECLARE txid INT;
  SELECT tx_id INTO txid FROM transaction WHERE tx_hash = tx LIMIT 1;
  CALL rec_follow_graph( txid, 0 );
END;
//

CREATE OR REPLACE PROCEDURE rec_follow_graph( tx BIGINT, depth INT )
`rec_follow_graph`:
BEGIN
  DECLARE done INT DEFAULT FALSE;
  DECLARE txid INT;
  DECLARE cur CURSOR FOR SELECT spent_by_tx FROM output WHERE tx_id = tx;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
  SELECT transaction.tx_hash FROM output LEFT JOIN transaction ON output.spent_by_tx = transaction.tx_id WHERE output.tx_id = tx;

  IF depth > 4 THEN
    LEAVE `rec_follow_graph`;
  END IF;

  OPEN cur;

  read_loop: LOOP
    FETCH cur INTO txid;
    IF done THEN
      LEAVE read_loop;
    END IF;
    call rec_follow_graph( txid, depth+1 );
  END LOOP;

  CLOSE cur;

END;

//
DELIMITER ;

