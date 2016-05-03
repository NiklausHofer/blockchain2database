CREATE TABLE IF NOT EXISTS multisig_signature(
  multisig_open_id BIGINT,
  signature_id BIGINT,
  idx INT,
    PRIMARY KEY(multisig_open_id,signature_id,idx),
    FOREIGN KEY(multisig_open_id) REFERENCES multisig_open(id),
    FOREIGN KEY(signature_id) REFERENCES signature(id)
)ENGINE = MEMORY;
