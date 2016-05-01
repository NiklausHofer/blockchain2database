CREATE TABLE IF NOT EXISTS out_script_p2pkh(
  tx_id BIGINT,
  tx_index INT,
  script_size INT,
  public_key_id BIGINT,
    PRIMARY KEY(tx_id, tx_index),
    FOREIGN KEY(tx_id) REFERENCES output(tx_id),
    FOREIGN KEY(tx_index) REFERENCES output(tx_index),
    FOREIGN KEY(public_key_id) REFERENCES public_key(id)
)ENGINE = MEMORY;
