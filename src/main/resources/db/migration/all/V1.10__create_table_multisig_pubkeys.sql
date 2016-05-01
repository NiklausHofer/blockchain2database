CREATE TABLE IF NOT EXISTS multisig_pubkeys(
  tx_id BIGINT,
  tx_index INT,
  public_key_id BIGINT,
  idx INT,
    PRIMARY KEY(tx_id, tx_index, public_key_id, idx),
    FOREIGN KEY(tx_id) REFERENCES output(tx_id),
    FOREIGN KEY(tx_index) REFERENCES output(tx_index),
    FOREIGN KEY(public_key_id) REFERENCES public_key(id)
)ENGINE = MEMORY;


