CREATE TABLE IF NOT EXISTS public_key(
  id BIGINT,
  pubkey_hash VARCHAR(64) UNIQUE,
  pubkey VARBINARY(520) UNIQUE,
    PRIMARY KEY(id)
)ENGINE = MEMORY;

CREATE INDEX public_key_hash ON public_key(pubkey_hash);
