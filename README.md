testdata_manager
================

Generate Google Charts for algorithm runtime testing

# Prerequisites

In order to use the scripts in this repository you need to deploy a mysql-server. This is needed because the code interacts with a MySql database
in which your test data will be stored. The charts will be generated from the tables of a chosen schema of the database. So after you succesfully deployed the mysql-server you have to create a database as well.

To install mysql-server run the following command:

```batch
sudo apt-get install mysql-server
```

After the installation open the mysql command prompt:
```batch
mysql -u root -p
(type in your mysql password)
```

Let's have a look at our databases:
```batch
mysql> show databases;
+--------------------+
| Database           |
+--------------------+
| information_schema |
| my_schema          |
| mysql              |
| performance_schema |
+--------------------+
```

Now we can create our own database for testing (e.g. test_db):
```batch
mysql> create database test_db;
```

Finally check whether the databse was created:
```batch
mysql> show databases;
+--------------------+
| Database           |
+--------------------+
| information_schema |
| my_schema          |
| mysql              |
| performance_schema |
| test_db            |
+--------------------+
```

# First steps

After you pulled the repository run the following scripts:

```batch
./initialize.sh
```

This will create several additional folders (logs, charts, config). In the config folder you have to give your own mysql setting.
The default path for new logs is already set for you. This is where you have to put the logs which you want to upload to the database.

# Notes

Right now only the following two feature is supported:

 * Multicast_ALS chart visualization
 * ALS runtime chart vizualization
 * PageRank runtime chart vizualization
