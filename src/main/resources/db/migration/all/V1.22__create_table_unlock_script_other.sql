CREATE TABLE IF NOT EXISTS unlock_script_other(
  tx_id BIGINT,
  tx_index BIGINT,
  script_size INT,
  script_id BIGINT,
    PRIMARY KEY(tx_id,tx_index),
    FOREIGN KEY(tx_id) REFERENCES input(tx_id),
    FOREIGN KEY(tx_index) REFERENCES input(tx_index),
    FOREIGN KEY(script_id) REFERENCES script(id)
)ENGINE = MEMORY;

