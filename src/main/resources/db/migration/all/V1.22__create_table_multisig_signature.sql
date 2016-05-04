CREATE TABLE IF NOT EXISTS multisig_signature(
  tx_id BIGINT,
  tx_index INT,
  signature_id BIGINT,
  idx INT,
    PRIMARY KEY(tx_id,tx_index,signature_id,idx),
    FOREIGN KEY(tx_id,tx_index) REFERENCES unlock_script_multisig(tx_id,tx_index),
    FOREIGN KEY(signature_id) REFERENCES signature(id)
)ENGINE = MEMORY;
