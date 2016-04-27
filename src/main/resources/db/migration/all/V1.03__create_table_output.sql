CREATE TABLE IF NOT EXISTS output(
  tx_id BIGINT,
  tx_index BIGINT,
  amount BIGINT,
  address VARCHAR(64),
  largescript BOOLEAN,
  spent_by_index BIGINT,
  spent_by_tx BIGINT,
  spent_at TIMESTAMP NULL,
    PRIMARY KEY(tx_id,tx_index),
    FOREIGN KEY(tx_id) REFERENCES transaction(tx_id)
)ENGINE = MEMORY;
