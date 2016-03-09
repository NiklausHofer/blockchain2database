--
-- Initialize Database Tables
--


CREATE TABLE block(
  blk_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  magic_id INT,
  difficulty INT,
  hash VARCHAR(35),
  prev_blk_id BIGINT,
  mkrl_root VARCHAR(35),
  time TIMESTAMP,
  transaction_count BIGINT,
  height BIGINT,
  version INT,
  nonce INT,
  output_amount BIGINT,
  input_amount BIGINT
)ENGINE = MEMORY;

CREATE TABLE transaction(
  tx_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  version INT,
  lock_time TIMESTAMP,
  blk_time TIMESTAMP,
  input_count INT,
  output_count INT,
  output_amount BIGINT, -- without transaction fee
  input_amount BIGINT,  -- 
  coinbase BOOL,
  blk_id BIGINT,
  tx_hash VARCHAR(),
  
  FOREIGN KEY(blk_id) REFERENCES block(blk_id)
  
)ENGINE = MEMORY;

CREATE TABLE output(
  output_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  amount BIGINT,
  tx_id BIGINT,
  tx_index INT,
  
  spent BOOL,
  spent_by_input BIGINT,
  spent_in_tx BIGINT,
  spent_at TIMESTAMP,
  
  FOREIGN KEY(tx_id) REFERENCES transaction(tx_id)
)ENGINE = MEMORY;

CREATE TABLE input(
  input_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  prev_output BIGINT,
  tx_index BIGINT,
  tx_id BIGINT,
  prev_tx_id BIGINT,
  prev_output_index INT,
  sequenze_number INT,
  amount BIGINT,  
  
  FOREIGN KEY(spending_output) REFERENCES output(output_id) ,
  FOREIGN KEY(tx_id) REFERENCES transaction(tx_id)
)ENGINE = MEMORY;

CREATE TABLE scrip(
  script_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  script_length BIGINT,
  script_code BLOB,
  input_id BIGINT,
  outpu_id BIGINT
  
)ENGINE = INNODB;

CREATE TABLE address(
  addr_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
  output_id BIGINT,
  addr_hash VARCHAR(35)
  
  FOREIGN KEY(output_id) REFERENCES output(output_id)
)ENGINE = MEMORY;

--
-- Connecting Relations
--

CREATE TABLE Block_transaction(
  blk_id BIGINT,
  tx_id BIGINT,
  PRIMARY KEY(blk_id,tx_id),
  
  FOREIGN KEY(blk_id) REFERENCES block(blk_id),
  FOREIGN KEY(tx_id) REFERENCES transaction(tx_id)
)ENGINE = MEMORY;

CREATE TABLE Block_transaction(
  addr_id BIGINT,
  output_id BIGINT,
)ENGINE = MEMORY;


