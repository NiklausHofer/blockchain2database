CREATE TABLE IF NOT EXISTS large_in_script(
  tx_id BIGINT,
  tx_index BIGINT,
  script_size BIGINT,
  script VARBINARY(${LARGE_INPUT_SCRIPT_SIZE}),
    PRIMARY KEY(tx_id,tx_index)
)ENGINE = INNODB;
