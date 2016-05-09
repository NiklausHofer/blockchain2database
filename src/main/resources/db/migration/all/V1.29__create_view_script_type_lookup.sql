CREATE VIEW script_type_lookup AS
  SELECT output.script_type_id AS script_type_id, out_script_other.script_id AS script_id
  FROM output
    RIGHT JOIN out_script_other
      ON output.tx_id = out_script_other.tx_id
      AND output.tx_index = out_script_other.tx_index
  UNION ALL
  SELECT input.script_type_id AS script_type_id, unlock_script_other.script_id AS script_id
  FROM input
    RIGHT JOIN unlock_script_other
      ON input.tx_id = unlock_script_other.tx_id
      AND input.tx_index = unlock_script_other.tx_index;
