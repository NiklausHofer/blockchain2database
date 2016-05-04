CREATE TABLE IF NOT EXISTS p2sh_multisig_signatures(
   tx_id BIGINT,
   tx_index INT,
   idx INT,
   signature_id BIGINT,
   PRIMARY KEY(tx_id,tx_index,idx,signature_id),
   FOREIGN KEY(signature_id) REFERENCES signature(id),
   FOREIGN KEY(tx_id,tx_index) REFERENCES unlock_script_p2sh_multisig(tx_id,tx_index)
)
ENGINE = MEMORY;