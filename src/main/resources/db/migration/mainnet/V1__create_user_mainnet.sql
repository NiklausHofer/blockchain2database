-- User for creating the database and inserting the data
CREATE USER IF NOT EXISTS 'mainnet'@'localhost' IDENTIFIED BY 'foobar';
GRANT RELOAD ON *.* TO 'mainnet'@'localhost';
GRANT ALL PRIVILEGES ON mainnet.* TO 'mainnet'@'localhost';
