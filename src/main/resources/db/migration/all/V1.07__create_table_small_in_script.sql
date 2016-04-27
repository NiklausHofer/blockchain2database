CREATE TABLE IF NOT EXISTS small_in_script(
  tx_id BIGINT,
  tx_index BIGINT,
  script_size BIGINT,
  script VARBINARY(${MAX_INMEMORY_INPUT_SCRIPT}),
    PRIMARY KEY(tx_id,tx_index)
)ENGINE = MEMORY;
