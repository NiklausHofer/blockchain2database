CREATE TABLE IF NOT EXISTS out_script_other(
  tx_id BIGINT,
  tx_index INT,
  script_size INT,
  script_id BIGINT,
    PRIMARY KEY(tx_id, tx_index),
    FOREIGN KEY(tx_id) REFERENCES output(tx_id),
    FOREIGN KEY(tx_index) REFERENCES output(tx_index),
    FOREIGN KEY(script_id) REFERENCES script(script_id)
)ENGINE = MEMORY;

