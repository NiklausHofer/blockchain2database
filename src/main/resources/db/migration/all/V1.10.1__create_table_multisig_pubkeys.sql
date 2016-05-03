CREATE TABLE IF NOT EXISTS multisig_pubkeys(
  multisig_lock_id BIGINT,
  public_key_id BIGINT,
  idx INT,
    PRIMARY KEY(multisig_lock_id, public_key_id, idx),
    FOREIGN KEY(multisig_lock_id) REFERENCES multisig_lock(id),
    FOREIGN KEY(public_key_id) REFERENCES public_key(id)
)ENGINE = MEMORY;


