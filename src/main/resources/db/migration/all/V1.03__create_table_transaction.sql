CREATE TABLE IF NOT EXISTS transaction(
  tx_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  version BIGINT,
  lock_time DATETIME DEFAULT 0,
  blk_time TIMESTAMP DEFAULT 0,
  blk_id BIGINT,
  tx_hash VARCHAR(64),
  blk_index BIGINT,
    FOREIGN KEY(blk_id) REFERENCES block(blk_id)
)ENGINE = MEMORY;

CREATE INDEX transaction_hash USING BTREE ON transaction (tx_hash);
