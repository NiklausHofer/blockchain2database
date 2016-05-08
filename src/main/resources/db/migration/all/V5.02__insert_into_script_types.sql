-- Script types. They are supposed to be the same values as in the ScriptType
-- enum found in
-- src/main/java/ch/bfh/blk2/bitcoin/blockchain2database/Dataclasses/ScriptType.java
INSERT INTO script_type (id, script_type) VALUES
  (00, 'OUT_P2PKHASH'),
  (01, 'OUT_P2RAWPUBKEY'),
  (02, 'OUT_MULTISIG'),
  (03, 'OUT_P2SH'),
  (04, 'OUT_OP_RETURN'),
  (05, 'OUT_OTHER'),
  (06, 'OUT_INVALID'),
  (07, 'IN_COINBASE'),
  (08, 'IN_P2PKH'),
  (09, 'IN_P2RAWPUBKEY'),
  (10, 'IN_MULTISIG'),
  (11, 'IN_P2SH_MULTISIG'),
  (12, 'IN_P2SH_OTHER'),
  (13, 'IN_OTHER'),
  (14, 'IN_INVALID'),
  (15, 'NO_PREV_OUT');
