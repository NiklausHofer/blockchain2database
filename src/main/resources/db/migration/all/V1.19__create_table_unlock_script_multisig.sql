CREATE TABLE IF NOT EXISTS unlock_script_multisig(
  tx_id BIGINT,
  tx_index INT,
  script_size INT,
  multisig_open_id BIGINT,
    PRIMARY KEY(tx_id,tx_index),
    FOREIGN KEY(multisig_open_id) REFERENCES multisig_open(id),
    FOREIGN KEY(tx_id) REFERENCES input(tx_id),
    FOREIGN KEY(tx_index) REFERENCES input(tx_index)
)ENGINE = MEMORY;
