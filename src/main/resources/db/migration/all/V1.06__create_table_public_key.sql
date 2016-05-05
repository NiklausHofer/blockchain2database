CREATE TABLE IF NOT EXISTS public_key(
  id BIGINT AUTO_INCREMENT,
  pubkey_hash VARCHAR(64) UNIQUE,
  pubkey VARBINARY(520) UNIQUE,
    PRIMARY KEY(id)
)ENGINE = MEMORY;

CREATE INDEX public_key_hash ON public_key(pubkey_hash);
CREATE INDEX public_key ON public_key(pubkey);
