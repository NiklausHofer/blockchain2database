-- User for creating the database and inserting the data
CREATE USER IF NOT EXISTS 'unittest'@'localhost' IDENTIFIED BY 'foobar';
GRANT RELOAD ON *.* TO 'unittest'@'localhost';
GRANT ALL PRIVILEGES ON unittest.* TO 'unittest'@'localhost';
