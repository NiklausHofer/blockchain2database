CREATE TABLE IF NOT EXISTS unlock_script_p2sh_multisig(
  tx_id BIGINT,
  tx_index INT,
  script_size INT,
  multisig_lock_id BIGINT,
  multisig_open_id BIGINT,
  PRIMARY KEY(tx_id,tx_index),
  FOREIGN KEY(tx_id,tx_index) REFERENCES input(tx_id,tx_index),
  FOREIGN KEY(multisig_lock_id) REFERENCES multisig_lock(id),
  FOREIGN KEY(multisig_open_id) REFERENCES multisig_open(id)
)ENGINE = MEMORY;