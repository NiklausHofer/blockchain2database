# Blockchain to Database

Blockchain to Database (Blk2DB) is a software to convert the Bitcoin Blockchain
into a relational database. The design of the database is flexible enough to
allow you to query virtually any aspect of the data within the blockchain.

Note that this software is not a Bitcoin client. It can not retrieve the
blockchain on it's own, so you will need a Bitcoin client and an up-to-date topy
of the blockchain.

## Requirements

To run the software, you will need a couple of software components, that you can
find listed below. Please note that if you plan on loading in the entire Bitcoin
Mainnet Blockchain, you will also want a very fast computer with a fast
database. We have done our initial readin to an in-memory MariaDB database and
it still took almost 10 days.

### Software components

 * [Java 8](http://openjdk.java.net/install/index.html)
 * [Flyway](https://flywaydb.org/)
 * [MariaDB 10.1+](https://mariadb.org/)

To retrieve and update the blockchain, you will also want a

 * [Bitcoin client](https://bitcoinclassic.com/)

### Compile requirements

If you want to build the software yourself, or work with it, you will need a
couple of additional tools

 * [Java 8 compiler](http://openjdk.java.net/install/index.html)
 * [Maven](https://maven.apache.org/)

You will probably also want

 * [Git](https://git-scm.com/)

However, if you'll be using Maven, you won't necessarily need Flyway.

## Testing / Development setup

If you want to develop Blk2DB or if you just want to check it out and toy
around, you can use Maven for pretty much anything. However, if you plan on
using the software for more serious purposes, you should probably get an
executable version and write propper configuration files.

### Configuration

There are a couple of important configuration files for Blk2DB: 

 * Database configuration [db.properties](src/main/resources/db.properties)
 * Blockchain configuration [blockchain.properties](src/main/resources/blockchain.properties)
 * Flyway configuration [flyway.properties](src/main/resources/flyway.properties)

And then there are also these:

 * Configuration for Unit tests [test_db.properties](src/main/resources/test_db.properties)
 * Log4j connfiguration [log4j2.xml](src/main/resources/log4j2.xml)

All of them can be found in [src/main/resources/](src/main/resources). Let's
have a quick look at all of them.

#### db.properties
This file is used by the software to connect to the database. Here are the
values you should set in this file:

 * *dbdriver*: The driver to be used. You probably want to leave this at
   `org.mariadb.jdbc.Driver`
 * *dburl*: This is the JDBC URL to connect to the database, including both
   the host and the specific database
 * *user*: The database user. The user needs write access on the database
   configured in the dburl parameter
 * *password*: The user's password

#### blockchain.properties
There are only two values here:

 * *directory*: The directory where the blockchain can be found. This is
  typically something like `/home/username/.bitcoin/blocks` or
  `/home/username/.bitcoin/testnet3/blocks`, depending on the network you're
  using
 * *testnet*: Boolean value, indicating wheter you want to operate on the
   testnet3 Bitcoin network. This is important because certain network
   parameters (such as address prefixes) depend on this setting

#### flyway.properties
This is, as you've probably guessed already, the configuration file for Flyway.
You can configure Flyway's database user sepparately, since it will need to be
able to create a new database.

* *flyway.user*: The database user to be used by flyway
* *flyway.password*: That user's password

<!-- TODO: rest of parameters -->

#### test_db.properties
You will want to configure a different database for the unit tests than for the
rest of the program. This database will be whiped an recreated several times
during unit tests, so be careful with this setting.

Other than that, it is exactly the same as db.properties.

### Flyway

Once you have written the flyway config file, you can run Flyway straight from
Maven. Please note, that Flyway is *not* run during any of the regular Maven
build phases. This means that every time you want to run Flyway, you have to
specifically tell Maven to do so.

You can read details on the usage of Flyway on their
[website](https://flywaydb.org/), but for now, it will do if you can create and
delete the database:

```
# Initialize the database
~$ mvn flyway:migrate

# Delete the database
~$ mvn flyway:clean
```

If you want to do this on the testnet3 database rather than the mainnet databae,
simply add @testnet3 to the end of the command:

```
# Initialize the database
~$ mvn flyway:migrate@testnet3

# Delete the database
~$ flyway:clean@testnet3
```

### Configuring MariaDB

Without any change, our scripts will create the database with the MEMORY storage
engine. 
