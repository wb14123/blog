---
layout: post
title: Keep Data Consistency During Database Migration
tags: [database, distributed system, consistency]
---

When a system has been live for a long time, it's not rare to use newer technologies to improve performance, maintainability, or add new features. One of such changes can be which database to use. This can be the most difficult kind of change. During the migration, there are two data sources, which makes it a distributed system. Make data consistent under a distributed system is very hard and can easily go wrong. In this article, we will explore a way to keep the data consistent during the migration, and maintain a low downtime at the same time.

## Requirements

There are some requirements in order to use the way described in this article:

* The source database support capture data change (CDC) method like MySQL bin log.
* The source database can be dumped with a consistent view and mark the position in data change logs.
* The target database support ACID transactions.
* Both source and target database support read and write permission control.

## Steps

There are two basic ideas behind the steps:

1. The clients only write to one of the databases at a given time. So we can avoid distributed transaction which is error prone and slow.
2. We make the switch of database by setup the database permissions. It's faster than switch from client code and easier to make sure to switch all the clients.

Here are the detailed steps:

### 1. Dump the source database to target database

First, we need to dump the source database with a consistent view. And mark the position we've dumped. For example, in MySQL, you can use `mysqldump` with `--master-data` to dump the database with a bin log position. ([Document about the usage](https://dev.mysql.com/doc/refman/8.0/en/mysqldump.html#option_mysqldump_master-data)). After we get all the data from source database, we can insert them into the target database.

Since this is the first step, it's very easy to handle failure: just start again from beginning. So it's very important to capture any error while import the dumped data.

### 2. Capture changes from source to target

The next step is to use the capture data changes from the source database. For example, in MySQL, you can use [bin log](https://dev.mysql.com/doc/refman/8.0/en/binary-log.html) to capture the changes and insert them to the target database. Since we have the start position from last step, we know where to start parse and import the changes.

It's very important to keep order of the changes while importing. So it's better to use only one process to parse and import the changes. This part is challenging: the performance matters here. **The time to sync all the changes is the downtime we need for migration**.

We also need to make sure we don't miss any changes or import any changes multiple times even there are system failures. So it's very important to record the change log position. It's convenience to write the position into the target database with the same transaction that imports the data. So the position will be synced with the data we imported.

### 3. Deny writes to the target database from clients

The easy way to keep data consistency is to have a single source of truth. Until now, we are using the source database as the source of truth and sync changes to the target database. We don't want to mess up the target database with other writes. So we need to setup the target database permission to deny all the writes from clients. For example, in MySQL, you can grant only `select` permission to the table for the clients and deny other operations. We allow the read permission so that we can compare the read results at the next step.

### 4. Modify the clients to read and write both databases

The next step is to make the clients to read and write both source and target databases.

We want to read/write source database first. Use this result if there is no permission error, use the read/write result from target database otherwise.

The read/write to target database has two purposes:

1. Before switch to the target database, we can verify the target database works as expected by compare read results and write operations. Note that the target database may have lag to sync up, so the results may not always the same. But we can have an understanding of the correctness based on the percentage of same results.
2. After we switch to the target database, the read/write results will be used as the real results.

**If you want to make sure the target database can handle the load, it's a good idea to allow read/write to the target database for a while .But it's just as a verification, the data in the target database will not be consistent after that. So after we verify the target database can handle the traffic, we need to cleanup the target database and start from step 1 again.** (We don't need to modify the client code during the steps).

For the error handling, there are two key points:

1. Only use target database result if there is permission error from source database. Throw other errors from source database.
2. Ignore errors for the target database if the result is not used but make sure to log them, so that it will not affect the current operation while also make sure we don't have errors before the switch.

The client code would be like this:

```
db_operation() {
  try {
    source_result = source_db_operation()
  } catch (PermissionException e) {
    return target_db_operation()
  }
  async {
    // do the following things async so it will not impact the performance
    try {
      target_result = target_db_operation()
      compare_result(source_result, target_result)
    } catch (Exception e) {
      log_error(e)
    }
  }
  return source_result
}
```

### 5. Deny access to the source database from clients and wait for changes to by synced

After we are confident with the read and write to the target database, we can make the switch. We switch the database by change the database permissions. First, we deny all the access to the source database from clients. Then we wait for the changes to be full synced to the target database. During this time, the system is down. So how fast the changes are synced from source database to target database determines how much down time it will be.

### 6. Allow write to target database

After the target database is fully synced, we can enable the target database permission for all the clients. After this, the system should be online again and the database is fully switched over.


### 7. Optional: Fallback to source database if anything goes wrong

It's good if everything works well so far. But that may not always the case. Maybe the target database cannot handle the new traffic (that's why it's important to test it in step 4). In this case, we need fallback to the source database.

If it's fine to lost committed data during the migration time, it would be relatively easy to fallback:

1. Allow access to the source database. After this, the clients should be using the source database again.
2. Cleanup the target database and start from the beginning.

If it's critical to save the committed data and make sure they are consistent, then before step 5, we should setup a mechanism to capture changes from target database to source database, and mark the change position after step 6. Then the fallback steps would be:

1. Deny all the writes to target database.
2. Sync from target database to source database (make sure to stop it after fully synced).
3. Allow access to the source database.
4. Cleanup the target database and start from the beginning again.

The sync from target database to source database is very dangerous and hard to test, so it's really important to test the target database can handle the operations in step 4.

### 8. Cleanup client code

Once the database is switched to the target database, we can cleanup the code that access the source database. Then the database is fully migrated and you can enjoin it!
