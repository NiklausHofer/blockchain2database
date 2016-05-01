CREATE TABLE IF NOT EXISTS input(
  tx_id BIGINT,
  tx_index INT,
  prev_tx_id BIGINT,
  prev_output_index BIGINT,
  sequence_number BIGINT,
  amount BIGINT,
  script_type_id INT,
    PRIMARY KEY(tx_id,tx_index),
    FOREIGN KEY(tx_id) REFERENCES transaction(tx_id),
    FOREIGN KEY(script_type_id) REFERENCES script_type(id)
)ENGINE = MEMORY;
