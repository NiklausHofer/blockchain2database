CREATE TABLE IF NOT EXISTS unlock_script_p2pkh(
  tx_id BIGINT,
  tx_index BIGINT,
  script_size INT,
  pubkey_id BIGINT,
  signature_id BIGINT,
    PRIMARY KEY(tx_id,tx_index),
    FOREIGN KEY(tx_id) REFERENCES input(tx_id),
    FOREIGN KEY(tx_index) REFERENCES input(tx_index),
    FOREIGN KEY(pubkey_id) REFERENCES public_key(id),
    FOREIGN KEY(signature_id) REFERENCES signature(id)
)ENGINE = MEMORY;

