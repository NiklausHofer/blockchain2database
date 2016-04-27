CREATE TABLE IF NOT EXISTS input(
  tx_id BIGINT,
  tx_index BIGINT,
  prev_tx_id BIGINT,
  prev_output_index BIGINT,
  largescript BOOLEAN,
  sequence_number BIGINT,
  amount BIGINT,
    PRIMARY KEY(tx_id,tx_index),
    FOREIGN KEY(tx_id) REFERENCES transaction(tx_id)
)ENGINE = MEMORY;
