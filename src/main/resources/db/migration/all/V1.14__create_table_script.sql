-- Use in test only
--CREATE TABLE IF NOT EXISTS script(
--  script_id BIGINT,
--  script_index INT,
--  op_code INT,
--  data VARBINARY(2048),
--    PRIMARY KEY(script_id, script_index)
--)ENGINE = InnoDB;

-- for production use
CREATE TABLE IF NOT EXISTS script(
  script_id BIGINT,
  script_index INT,
  op_code INT,
  data VARBINARY(4096),
    PRIMARY KEY(script_id, script_index),
    FOREIGN KEY(op_code) REFERENCES op_codes(op_code)
)ENGINE = InnoDB;
