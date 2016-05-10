CREATE TABLE IF NOT EXISTS signature(
  id  BIGINT AUTO_INCREMENT,
  signature VARCHAR(152),
  pubkey_id BIGINT,
    PRIMARY KEY(id),
    FOREIGN KEY(pubkey_id) REFERENCES public_key(id)
)ENGINE = MEMORY;
