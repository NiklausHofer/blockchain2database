-- OUTGOING TRANSACTIONS OF AN ADDRESS
--DELIMITER //
--CREATE PROCEDURE outgoing_addr_tx
--(IN con VARCHAR(35)) 
--  BEGIN
--    SELECT transaction.tx_id, transaction.tx_hash
--    FROM transaction,
--      output INNER JOIN input ON 
--      input.prev_tx_id = output.tx_id
--      AND input.prev_output_index = output.tx_index
--    WHERE output.address = addr
--    AND input.tx_id = transaction.tx_id;
--  END;
--//
--DELIMITER ;