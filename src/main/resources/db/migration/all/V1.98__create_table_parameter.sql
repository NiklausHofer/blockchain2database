CREATE TABLE IF NOT EXISTS parameter(
   p_key VARCHAR(64),
   p_value VARCHAR(64) NOT NULL,
     PRIMARY KEY(p_key)
) ENGINE = InnoDB;
