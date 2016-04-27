-- User for creating the database and inserting the data
CREATE USER IF NOT EXISTS 'testnet3'@'localhost' IDENTIFIED BY 'foobar';
GRANT RELOAD ON *.* TO 'testnet3'@'localhost';
GRANT ALL PRIVILEGES ON testnet3.* TO 'testnet3'@'localhost';
