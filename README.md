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
engine. Default MariaDB configurations do not allow large in-memory databases.
So you will need to edit `/etc/mysql/my.cnf` and set at least these variables:
```
max_heap_table_size=256G
tmp_table_size=256G
```

Whilest you are at it, you will probably also want to optimize some other
settings such as `key_buffer_size`, `sort_buffer_size` and others. General
optimization of a MariaDB instance is out of the scope of this REAME though.
Please consult a MariaDB optimization guide for that instead.

#### WARNING

Changing MariaDB configuration might require you to restart the service. The
MEMORY tables will get deleted on each restart of MariaDB. If you want to avoid
data loss, you have to create a Dump of the data first. If you have to regularly
restart the service or if you can't trust your MEMORY (for example, if you don't
have ECC Memory), you should probably use a different storage engine.

In our experience, it's a good idea, to use the MEMORY storage engine for the
initial readin of the data which can take a long time and causes high load on
the DBMS and then switch over to InnoDB for daily use.

### Using InnoDB instead

If you don't have the necessary amount of memory in your system, you can switch
to another MariaDB storage engine. We have tested the software to work with
InnoDB. If you do this, you will want your InnoDB database to be stored on an
SSD or similarly fast storage backend.

To change the storage engine, you have to edit the SQL files, since the used
storage engine is specified explicitly in each file. Note that you need to do
that *before* the first run of Flyway, otherwise you will confuse it.

You can change all the storage engine definitions with something like the
command below. Keep in mind, that this example is specific to [GNU
find](https://www.gnu.org/software/findutils/) and [GNU
sed](https://www.gnu.org/software/sed/) and the arguments might be different for
different implementations of these utilities (such as under BSD).
```
~$ find src/main/resources/db/migration/ -name "*.sql" -exec sed -i -e's/ENGINE\s*=\s*MEMORY/ENGINE = InnoDB/g' {} \
```

### Compiling and running the software

You can compile the software using the usual Maven workflow. For the Unit tests
to work, the `test_db.properties` needs to be configured. The unit tests have
most of their Flyway configuration hardcoded in the
[DBManager.java](src/test/java/ch/bfh/blk2/bitcoin/blockchain2database/DBManager.java)
class, however, they still load the username and password used to administrate
the database from the flyway.properties. If you don't want to build the software
without running the tests in the process, use Maven like this:
```
mvn -Dmaven.test.skip=true clean package
```

Compiling the software with Maven will also produce an executable JAR.

You can use Maven to directly run the software. If you do that, you will
probably don't want to run the tests either. Once you have written all the
configuration files and readied the database, you can run the Blockchain to
Database software with this Maven command:
```
mvn -Dmaven.test.skip=true clean package exec:java -Dexec.mainClass="ch.bfh.blk2.bitcoin.blockchain2database.Blk2DB"

```


## Production use

To use the software in a production environment where you will want to run it in
regular intervals to update the database, we recommend you use an executable
JAR. The JAR gets created when you compile the software using Maven. It contains
all the configuration files, so you could edit them before compiling the
software. This however, is probably undesireable. To give you more flexibility,
we allow overriding the config files from the outside. To do that, either put
the configuration files, with the same name, in either
`/etc/blockchain2database` or in the current working directory. Blk2DB will
prefer configuration files in these locations over the one it has compiled in.
It also prefers those configuration files over the ones found in the classpath,
so you will want to keep your development environment and production environment
separate to avoid undesired effects when your development instance uses the
stable configuration.

### fileMap

### Flyway

### Cronjob


## Webinterface


## License

This software is published under the [GNU PGL
v3](https://www.gnu.org/licenses/gpl-3.0.en.html). All copyright remains with
the original authors.
