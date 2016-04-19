CREATE USER IF NOT EXISTS 'mainnet'@'localhost' IDENTIFIED BY 'foobar';
DROP DATABASE IF EXISTS btc;
CREATE DATABASE btc;
GRANT ALL PRIVILEGES ON btc.* TO 'mainnet'@'localhost';

