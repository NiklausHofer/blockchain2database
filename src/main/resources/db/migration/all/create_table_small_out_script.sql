CREATE TABLE IF NOT EXISTS small_out_script(
  tx_id BIGINT,
  tx_index BIGINT,
  script_size BIGINT,
  script VARBINARY(${MAX_INMEMORY_OUTPUT_SCRIPT}),
  isOpReturn BOOLEAN,
  isPayToScriptHash BOOLEAN,
  isSentToAddress BOOLEAN,
  isSentoToMultiSig BOOLEAN,
  isSentToRawPubKey BOOLEAN,
    PRIMARY KEY(tx_id,tx_index)
)ENGINE = MEMORY;

