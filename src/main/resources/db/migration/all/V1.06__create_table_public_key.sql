-- According to the Bitcoin Wiki, a public key is at most 65 Byte in size
CREATE TABLE IF NOT EXISTS public_key(
  id BIGINT AUTO_INCREMENT,
  pubkey_hash VARCHAR(64),
  pubkey VARCHAR(192),
  valid_pubkey BOOLEAN,
    PRIMARY KEY(id)
)ENGINE = MEMORY;

-- According to the MariaDB documentation, this will make
-- the rows unique.
-- https://mariadb.com/kb/en/mariadb/getting-started-with-indexes/#unique-index
CREATE UNIQUE INDEX pubkey_hash USING BTREE ON public_key(pubkey_hash);

-- We don't actually need this index, do we...
--CREATE UNIQUE INDEX pubkey USING BTREE ON public_key(pubkey);
