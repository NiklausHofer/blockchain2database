CREATE TABLE IF NOT EXISTS unlock_script_coinbase(
  tx_id BIGINT,
  tx_index BIGINT,
  script_size INT,
  information VARBINARY(64),
    PRIMARY Key(tx_id,tx_index),
    FOREIGN KEY(tx_id) REFERENCES input(tx_id),
    FOREIGN KEY(tx_index) REFERENCES input(tx_index),
)ENGINE = MEMORY;
