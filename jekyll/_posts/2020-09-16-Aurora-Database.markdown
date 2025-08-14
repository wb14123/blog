---
layout: post
title: Aurora Database
tags: [Aurora, distributed system, database, 2PC]
index: ['/Computer Science/Distributed System']
---

I'm very interested in databases and distributed systems. It's shocking how few in-depth articles about databases are on the Internet. I wrote an [article about Spanner](/2018-07-29-A-Review-on-Spanner-and-Open-Source-Implementations.html) before and I'm very satisfied with that. So recently, I'm looking at another interesting database [Aurora](https://aws.amazon.com/rds/aurora/). This article is about it.

Aurora is a cloud based database from Amazon. It's not as fancy as Spanner, which provides serializable and linearizable in a globally distributed system. Aurora is much more practical. In real life, most businesses don't need global distributed database. They only need a database that can survive from a data center failure. In this case, Aurora can provide much higher throughput and much lower latency.

In this article, we will have a look at how Aurora implements transaction in a distributed system. And then we talk about how it gets a better performance than setups like MySQL replication.

The discussion of this article is based on the [AWS Aurora user manual](https://docs.aws.amazon.com/AmazonRDS/latest/AuroraUserGuide/CHAP_AuroraOverview.html) and 2 papers that describe Aurora architecture: 

* [Amazon Aurora: Design Considerations for High Throughput Cloud-Native Relational Databases](https://media.amazonwebservices.com/blog/2017/aurora-design-considerations-paper.pdf)
* [Amazon Aurora: On Avoiding Distributed Consensus for I/Os, Commits, and Membership Changes](https://dl.acm.org/doi/abs/10.1145/3183713.3196937)

## Basic Properties of Aurora

Aurora has both single master and multi-master setup. Multi-master setup provides weaker isolation levels, and there are less papers and resources about it. So we will focus on single master setup in this article. With this single master setup, Aurora can provide serializable isolation.

By master node, I mean the database node in Aurora. Aurora has a database node and multiple storage nodes. The database node receives client requests and sends WAL logs to each storage node. Storage nodes process WAL logs and write buffers/pages independently.

## How Transaction Is Implemented

The most interesting part of a distributed system is how it keeps data consistent. This is the most difficult and error-prone part:  if the performance is bad, it's obvious. But if the transaction is implemented in the wrong way, the data looks good at most of the time. Once the race condition is triggered, the data is corrupt and it's very hard to debug and find the reason.

Because Aurora is a single master system, the transaction implementation is relatively easy.

In the case of writes, the database instance generates monotonically increasing IDs for WAL log entries and sends them to storage instances. Storage instances process the logs in the order of IDs. Once the storage instance parses a log entry, it will send a response to the database instance. After the database instance receives the responses for all the log entries in a transaction, it will mark this transaction as committed and respond to the client.

In the case of reads, because the database instance keeps track of all the log IDs and transactions, it knows the most recent log ID for a committed transaction. The database instance also tracks which storage instances have parsed which log entries. So it can use this log ID to query a snapshot on storage instance.

## How Quorum Is Used

When the database instance sends WAL logs to storage instances, it doesn't wait for all of them to respond to consider the write as successful. Instead, it only needs the confirmation from some of them. This is called a write quorum. In Aurora, each data segment has 6 nodes distributed among 3 availability zones. The write quorum for Aurora is 4. Which means the writes are persistent on at least 2 availability zones.

Why a write quorum is enough instead of all the nodes? Let's introduce the read quorum. Read quorum means how many nodes you need to read when querying the data. As long as `read quorum + write quorum > number of nodes`, there will be an overlapping node between the read quorum and write quorum, which ensures we will always read the newest data. In Aurora, read quorum is set to 3.

However, since Aurora tracks all the log IDs and storage nodes status, it knows which storage node has the newest data, so it doesn't really need a read quorum for every read request. Instead, the read quorum is used in failure recovery.

## How Failure is Handled

### Storage Node Failure

Since Aurora has multiple storage nodes and uses write quorum to ensure the data is written to most of them, it's trivial for storage node failure handling: as long as there are no more than 2 storage nodes failed, the database can still work. As long as no more than 3 storage nodes failed, the database can still handle read requests. Because it has 2 nodes in each availability zone, it means it can survive from failure of 1 availability zone. Once a storage node is failed, a new storage instance will start and copy data from other storage nodes.

In addition, Aurora also backs up data to S3. So even if the database is totally destroyed, it can still recover a snapshot.

### Database Node Failure

Since there is only one database node, it's very important to handle its failure. The database node maintains the log ID for the latest completed transaction, which is critical to maintain transaction correctness. In case of database node failure, Aurora recovers this ID from storage nodes: each storage node knows the log ID for its latest transaction, so with a read quorum, it can get the latest transaction ID across the cluster.

There is a tricky part here: this may recover the transaction that has written successfully to a write quorum, but never responded to the database node and client. This is okay since if the client lost connection to the database and doesn't get a response, the commit can either be successful or failed. This is true for all database systems because there is just no way to deal with that. A deeper discussion is out of topic and involves [two general's problem](https://en.wikipedia.org/wiki/Two_Generals%27_Problem).

## Why Aurora Can Avoid the Usage of Two Phase Commit

Usually, in a distributed system, in order to maintain data consistency, we need to use some protocols like two phase commit (2PC) or Paxos. In 2PC, there is a coordinator node that receives all client requests. Then it asks all the storage nodes whether they can handle the request (phase 1). If all the nodes processed the request to a commit point (but not commit) and respond they can handle it, the coordinator will send a commit request to all the nodes (phase 2), and mark it as complete once storage nodes complete the commit. If there is any storage node responding it cannot handle the request in phase 1, the coordinator node will send a rollback request to each node. And mark the transaction as failed once all storage nodes rolled back the changes.

2PC is slow because it needs 2 rounds of requests for all nodes. Once a node responds they can write the data in phase 1, it must do that. Even if the node fails, it must recover and write it. Otherwise the system cannot continue to handle further requests.

I'm always skeptical when people claim they don't need data consistency protocols like Paxos and 2PC. It's also not clear in the first paper how Aurora is able to avoid it. However, in the second paper, there is a key sentence that explains that:

"This is possible because storage nodes do not have a vote in determining whether to accept a write, they must do so. Locking, transaction management, deadlocks, constraints, and other con-ditions that influence whether an operation may proceed are all resolved at the database tier."

This means database node already knows whether storage nodes can handle the write or not. So phase 1 in 2PC is not needed. It's not clear whether it needs to request storage node to resolve these things, but even it's needed, it only needs to request one storage node instead of all of them. In short, Aurora can avoid 2PC because the database node knows and does much more than the coordinator node in 2PC, and storage nodes store the same data so it doesn't need to request all of them.

## How Performance Is Improved

We've talked a lot about transaction management of Aurora. But the true contribution of Aurora is the performance improvement. It is accomplished in multiple ways:

First of all, it only replicates WAL logs. This saves lots of network bandwidth even though it means each node needs to do more computation. Apparently, network bandwidth is more important in AWS while it's not necessarily true in other systems. So it's very important to know the bottleneck and optimize with purpose.

Second, as we said before, Aurora avoids slow protocols like 2PC.

At last, Aurora makes the system async when possible. You can find examples everywhere:

* When database nodes are handling client requests, they don't block and wait for responses from storage nodes. They put work in a queue and respond to the client after enough storage nodes have responded.
* When storage nodes are receiving logs, they don't receive them one by one. Instead, they receive them without a specific order but order them while parsing.
* When storage nodes are writing data, the write is considered successful as long as the WAL log is persistent. They can process the logs and write buffers and pages in the background.

System designs are trade-offs. I'm not a fan of sacrificing data consistency for performance: people don't always make the right assumptions about consistency requirements. Aurora doesn't do that. Instead, it builds a single master system with multiple slave nodes that are connected by a low latency network. I think Aurora did a great job to get the sweet spot between availability and performance.
