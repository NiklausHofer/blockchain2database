CREATE TABLE IF NOT EXISTS block(
  blk_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  difficulty BIGINT,
  hash VARCHAR(64),
  prev_blk_id BIGINT,
  mrkl_root VARCHAR(64),
  time TIMESTAMP DEFAULT 0,
  height BIGINT,
  version BIGINT,
  nonce BIGINT
)ENGINE = MEMORY;
