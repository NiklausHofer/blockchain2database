CREATE TABLE IF NOT EXISTS unlock_script_p2sh_multisig(
  tx_id BIGINT,
  tx_index INT,
  script_size INT,
  min_keys INT,
  max_keys INT,
  PRIMARY KEY(tx_id,tx_index),
  FOREIGN KEY(tx_id,tx_index) REFERENCES input(tx_id,tx_index)
)ENGINE = MEMORY;