CREATE TABLE IF NOT EXISTS multisig_pubkeys(
  public_key_id BIGINT,
  idx INT,
  tx_id BIGINT,
  tx_index INT,
    PRIMARY KEY(tx_id,tx_index, public_key_id, idx),
    FOREIGN KEY(public_key_id) REFERENCES public_key(id),
    FOREIGN KEY(tx_id,tx_index) REFERENCES out_script_multisig(tx_id,tx_index)
)ENGINE = MEMORY;