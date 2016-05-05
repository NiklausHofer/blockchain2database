CREATE TABLE IF NOT EXISTS signature(
  id  BIGINT,
  signature VARBINARY(65),
  pubkey_id BIGINT,
    PRIMARY KEY(id),
    FOREIGN KEY(pubkey_id) REFERENCES public_key(id)
)ENGINE = MEMORY;
