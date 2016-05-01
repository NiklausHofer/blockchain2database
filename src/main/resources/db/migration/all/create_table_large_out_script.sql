CREATE TABLE IF NOT EXISTS large_out_script(
  tx_id BIGINT,
  tx_index BIGINT,
  script_size BIGINT,
  script VARBINARY(${LARGE_OUTPUT_SCRIPT_SIZE}),
  isOpReturn BOOLEAN,
  isPayToScriptHash BOOLEAN,
  isSentToAddress BOOLEAN,
  isSentoToMultiSig BOOLEAN,
  isSentToRawPubKey BOOLEAN,
    PRIMARY KEY(tx_id,tx_index)
)ENGINE = INNODB;
