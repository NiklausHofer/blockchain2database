--
-- Create Mainnet database
--

-- The following command is destructive - careful with that
--DROP DATABASE IF EXISTS mainnet;
CREATE DATABASE IF NOT EXISTS mainnet;

--
-- Create testnet database
--
--DROP DATABASE IF EXISTS testnet3;
CREATE DATABASE IF NOT EXISTS testnet3;

--
-- Create the users for mainnet
--

-- User for creating the database and inserting the data
CREATE USER IF NOT EXISTS 'mainnet'@'localhost' IDENTIFIED BY 'foobar';
GRANT RELOAD ON *.* TO 'mainnet'@'localhost';
GRANT ALL PRIVILEGES ON btc.* TO 'mainnet'@'localhost';

-- Strongly limited web frontend user
CREATE USER IF NOT EXISTS 'pubweb'@'localhost' IDENTIFIED BY 'foobar';

GRANT SELECT, SHOW VIEW ON TABLE mainnet.block TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 5;
GRANT SELECT, SHOW VIEW ON TABLE mainnet.input TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 5;
GRANT SELECT, SHOW VIEW ON TABLE mainnet.large_in_script TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 5;
GRANT SELECT, SHOW VIEW ON TABLE mainnet.large_out_script TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 5;
GRANT SELECT, SHOW VIEW ON TABLE mainnet.output TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 5;
GRANT SELECT, SHOW VIEW ON TABLE mainnet.parameter TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 5;
GRANT SELECT, SHOW VIEW ON TABLE mainnet.small_in_script TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 5;
GRANT SELECT, SHOW VIEW ON TABLE mainnet.small_out_script TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 5;
GRANT SELECT, SHOW VIEW ON TABLE mainnet.transaction TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 5;

-- enable to execute stored procedures
GRANT EXECUTE ON PROCEDURE mainnet.tx_fee TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.tx_input_amount TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.tx_output_amount TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.get_utxo TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.outgoing_addr_tx TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.incomming_addr_tx TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.outgoing_addr_tx TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.rec_follow_graph TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.depend_on_tx TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.count_addr_with_balance_higher_than TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.count_addr_which_received_higher_than TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.block_output_amount TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.block_input_amount TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.block_fee TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.get_balance TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.outgoing_addr_tx_time TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;

-- Unlimited read only user
CREATE USER IF NOT EXISTS 'privweb'@'localhost' IDENTIFIED BY 'foobar';

GRANT SELECT, SHOW VIEW ON TABLE mainnet.block TO 'privweb'@'localhost';
GRANT SELECT, SHOW VIEW ON TABLE mainnet.input TO 'privweb'@'localhost';
GRANT SELECT, SHOW VIEW ON TABLE mainnet.large_in_script TO 'privweb'@'localhost';
GRANT SELECT, SHOW VIEW ON TABLE mainnet.large_out_script TO 'privweb'@'localhost';
GRANT SELECT, SHOW VIEW ON TABLE mainnet.output TO 'privweb'@'localhost';
GRANT SELECT, SHOW VIEW ON TABLE mainnet.parameter TO 'privweb'@'localhost';
GRANT SELECT, SHOW VIEW ON TABLE mainnet.small_in_script TO 'privweb'@'localhost';
GRANT SELECT, SHOW VIEW ON TABLE mainnet.small_out_script TO 'privweb'@'localhost';
GRANT SELECT, SHOW VIEW ON TABLE mainnet.transaction TO 'privweb'@'localhost';

-- enable to execute stored procedures
GRANT EXECUTE ON PROCEDURE mainnet.tx_fee TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.tx_input_amount TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.tx_output_amount TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.get_utxo TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.outgoing_addr_tx TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.incomming_addr_tx TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.outgoing_addr_tx TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.rec_follow_graph TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.depend_on_tx TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.count_addr_with_balance_higher_than TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.count_addr_which_received_higher_than TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.block_output_amount TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.block_input_amount TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.block_fee TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.get_balance TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE mainnet.outgoing_addr_tx_time TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;

-- Do this for all the stored procedures
--GRANT EXECUTE ON PROCEDURE mainnet.foo;

--
-- Create the users for testnet
--
-- User for creating the database and inserting the data
CREATE USER IF NOT EXISTS 'testnet3'@'localhost' IDENTIFIED BY 'foobar';
GRANT RELOAD ON *.* TO 'testnet3'@'localhost';
GRANT ALL PRIVILEGES ON btc.* TO 'testnet3'@'localhost';

-- Strongly limited web frontend user
CREATE USER IF NOT EXISTS 'pubweb'@'localhost' IDENTIFIED BY 'foobar';

GRANT SELECT, SHOW VIEW ON TABLE testnet3.block TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 5;
GRANT SELECT, SHOW VIEW ON TABLE testnet3.input TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 5;
GRANT SELECT, SHOW VIEW ON TABLE testnet3.large_in_script TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 5;
GRANT SELECT, SHOW VIEW ON TABLE testnet3.large_out_script TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 5;
GRANT SELECT, SHOW VIEW ON TABLE testnet3.output TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 5;
GRANT SELECT, SHOW VIEW ON TABLE testnet3.parameter TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 5;
GRANT SELECT, SHOW VIEW ON TABLE testnet3.small_in_script TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 5;
GRANT SELECT, SHOW VIEW ON TABLE testnet3.small_out_script TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 5;
GRANT SELECT, SHOW VIEW ON TABLE testnet3.transaction TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 5;

-- enable to execute stored procedures
GRANT EXECUTE ON PROCEDURE testnet3.tx_fee TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.tx_input_amount TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.tx_output_amount TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.get_utxo TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.outgoing_addr_tx TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.incomming_addr_tx TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.outgoing_addr_tx TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.rec_follow_graph TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.depend_on_tx TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.count_addr_with_balance_higher_than TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.count_addr_which_received_higher_than TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.block_output_amount TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.block_input_amount TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.block_fee TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.get_balance TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.outgoing_addr_tx_time TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME 90;

-- Unlimited read only user
CREATE USER IF NOT EXISTS 'privweb'@'localhost' IDENTIFIED BY 'foobar';

GRANT SELECT, SHOW VIEW ON TABLE testnet3.block TO 'privweb'@'localhost';
GRANT SELECT, SHOW VIEW ON TABLE testnet3.input TO 'privweb'@'localhost';
GRANT SELECT, SHOW VIEW ON TABLE testnet3.large_in_script TO 'privweb'@'localhost';
GRANT SELECT, SHOW VIEW ON TABLE testnet3.large_out_script TO 'privweb'@'localhost';
GRANT SELECT, SHOW VIEW ON TABLE testnet3.output TO 'privweb'@'localhost';
GRANT SELECT, SHOW VIEW ON TABLE testnet3.parameter TO 'privweb'@'localhost';
GRANT SELECT, SHOW VIEW ON TABLE testnet3.small_in_script TO 'privweb'@'localhost';
GRANT SELECT, SHOW VIEW ON TABLE testnet3.small_out_script TO 'privweb'@'localhost';
GRANT SELECT, SHOW VIEW ON TABLE testnet3.transaction TO 'privweb'@'localhost';


-- enable to execute stored procedures
GRANT EXECUTE ON PROCEDURE testnet3.tx_fee TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.tx_input_amount TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.tx_output_amount TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.get_utxo TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.outgoing_addr_tx TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.incomming_addr_tx TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.outgoing_addr_tx TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.rec_follow_graph TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.depend_on_tx TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.count_addr_with_balance_higher_than TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.count_addr_which_received_higher_than TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.block_output_amount TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.block_input_amount TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.block_fee TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.get_balance TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;
GRANT EXECUTE ON PROCEDURE testnet3.outgoing_addr_tx_time TO 'privweb'@'localhost' WITH MAX_STATEMENT_TIME 90;

