CREATE TABLE IF NOT EXISTS output(
  tx_id BIGINT,
  tx_index BIGINT,
  amount BIGINT,
  spent_by_index BIGINT,
  spent_by_tx BIGINT,
  spent_at TIMESTAMP NULL,
  script_type_id INT,
    PRIMARY KEY(tx_id,tx_index),
    FOREIGN KEY(tx_id) REFERENCES transaction(tx_id),
    FOREIGN KEY(script_type_id) REFERENCES script_type(id)
)ENGINE = MEMORY;

