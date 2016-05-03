CREATE TABLE IF NOT EXISTS out_script_multisig(
  tx_id BIGINT,
  tx_index INT,
  script_size INT,
  multisig_lock_id BIGINT,
    PRIMARY KEY(tx_id, tx_index),
    FOREIGN KEY(tx_id) REFERENCES output(tx_id),
    FOREIGN KEY(tx_index) REFERENCES output(tx_index),
    FOREIGN KEY(multisig_lock_id) REFERENCES multisig_lock(id)
)ENGINE = MEMORY;

