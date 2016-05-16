-- according to the Bitcoin Wiki, a signature is at most 73 Byte long
CREATE TABLE IF NOT EXISTS signature(
  id  BIGINT AUTO_INCREMENT,
  signature VARCHAR(152),
    PRIMARY KEY(id)
)ENGINE = MEMORY;
