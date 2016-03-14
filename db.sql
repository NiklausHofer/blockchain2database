--CREATE USER IF NOT EXISTS 'testnet3'@'localhost'  IDENTIFIED BY 'foobar';
DROP DATABASE IF EXISTS testnet3;
CREATE DATABASE testnet3;
GRANT ALL PRIVILEGES ON testnet3.* TO 'testnet3'@'localhost';

