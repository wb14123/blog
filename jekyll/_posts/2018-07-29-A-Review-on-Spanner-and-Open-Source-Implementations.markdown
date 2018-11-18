---
layout: post
title: Spanner and Open Source Implementations
tags: [database, technology, distributed system]
categories: Technical
---

When [Spanner paper](https://ai.google/research/pubs/pub39966) is published, the use of synced clock to implement globally distributed database attracted a lot of attentions. After these years, Google have put Spanner on its cloud to make everyone be able to use it. And there are also some open source implementations of Spanner in these years. In this article, I'd like to write about how does synced clock makes Spanner special, some notes of using it and how others implement it without special hardware clock.

## 1. Clock Is Not Trustable

Almost every computer has a clock on nowadays. A surprising fact is, the clock is not really accurate and normally we cannot even know the upper bound of the error. There are some reasons for this:

1. Normally the clock is synced with a remote server using NTP. But the network delay upper bound is unknown.
2. Even the local clock is synced accurately once, the hardware in normal computer makes it inaccuracy when time passes. The error is depends on the temperature and so on.

For more details, you can read the section "Unreliable Clocks" in Chapter 8 of the book [
Designing Data-Intensive Applications](http://shop.oreilly.com/product/0636920032175.do).

It is not a big deal in normal life to have an unreliable clock, but it is a big deal when your consistency algorithm relies on it. We can see the details in the next section.

## 2. What is TrueTime API?

In order to get rid of the unreliable of clocks, Google use some special hardware like atomic clock to make it reliable. The time which can get from each server is still inaccurate, but the error has a known upper bound. Specifically, these APIs are provided:

| Method | Returns |
|--------| --------|
| TT.now()     | A time interval: [earliest, latest]
| TT.after(t)  | true if `t` has definitely passed
| TT.before(t) | true if `t` has definitely not arrived

As we can see later, the error upper bound will effect the latency of each transaction. Google said they normally maintain it under 7ms and with an average value of 4ms in their Spanner paper.


## 3. What TrueTime API Gives Spanner?

What makes Spanner special is how it uses TrueTime API to do concurrency control. Spanner can guarantee external consistency, which is the most strong concurrency model. It means Spanner is both serializable and linearizable, which makes it very easy for application to avoid concurrency bugs. Serializable isolation can be found in many databases, but usually they are single leader databases, or have a centralized server. This makes it very slow if the database is globally distributed, since the network delay between data centers is large and unstable. If every transaction needs communicate with a centralized server, the transactions will be very slow and unstable.

Before understanding how Spanner can do this, let's first look at how serialization transaction can be implemented in a centralized way. The simplest way is let a transaction grant a lock if it want's to read or write a row, so that this row cannot be modified or read at the same time by another transactions. This is a relatively strong restraint, it will give us bad performance since every transaction excludes each other. We can have a better solution: for read only transactions, serializable isolation is the same as snapshot isolation. So we can implement snapshot isolation for read only transactions in order to have better performance.

Here is how snapshot isolation works: for every row of data, database stores not only the data, but also a version with it. When a transaction starts, it will only read data earlier than it. Normally we assign an id for each transaction and use it as the version. So here is the key: transaction id needs to be monotone increasing so that a later transaction can only read earlier data. Make a globally monotone increasing sequence is very straightforward and easy for a single machine, but it is very hard in a distributed environment without a centralized server.

Ordering transactions is complex but can be done without a clock. For example, the vector clock algorithm can do this. But this will not guarantee the accurate order of transaction. For example, if T1 commits before T2 start and they are operating on different partitions, the database may order T2 before T1. An implementation of Spanner Cockroachdb uses algorithms like this. We will analysis the problem of it in Section 6.2.

**So what TrueTime API really gives Spanner is generating monotone increasing transaction ID and guarantee external consistency ( or linearizability) at the same time**: if T1 commits before T2 start, then T1's transaction ID is always smaller than T2's transaction ID. Spanner implement this by getting a timestamp from TrueTime API that absolutely passed the current time, and wait to commit until currrent time absolutely passed the timestamp. In Spanner's paper, there is a proof that this can garantee external consistency:

$$ s_1 < t_{abs}(e_1^{commit}) $$ (commit wait)

$$ t_{abs}(e_1^{commit}) < t_{abs}(e_2^{start}) $$ (assumption)

$$ t_{abs}(e_2^{start}) \leq t_{abs}(e_2^{server}) $$ (causality)

$$ t_{abs}(e_2^{server}) \leq s_2 $$ (start)

$$ s_1 < s_2 $$ (transitivity)

There are also other parts of Spanner that use TrueTime API, for example, the Paxos implementation uses TrueTime API for leader lease. But it is just a implement optimization which can be done in other ways, too.

## 4. The Myth of Spanner

There are some myth and hyper for Spanner. In this section, I will highlight the weakness of Spanner. I call it "weakness" not because other databases are doing better, but because they are not as good as people may thought. And they are not necessary bad things. It depends on how you understand and use it. If you don't understand it correctly and thinks all the things are done perfectly and magically, you may run into some problems.

Many people think when using Spanner, reads can go to a replica closest to the client in most of the time, so the communication between data centers are avoid. But this is not true. In fact, **all the serializable transactions, which includes read-write transactions and read only transactions that wants to read the most recently data, must communicate with the partition leader.** The theory is in the Spanner paper and there is also an explicit description on [Spanner Cloud's document](https://cloud.google.com/spanner/docs/replication).

Let's first look at the read-write transaction. Since read-write transaction will write Paxos log, it is straightforward to understand it must involves the leader. And more, it must waits for the major of replicas to confirm the writes in order to make sure the write will not lost when leader fails.

Then let's look at the read-only transactions. In Spanner, every replica keeps a timestamp $$t_{safe}$$. And if a read-only want's to read the data at timestamp $$t$$ and if $$t \leq t_{safe}$$, it is safe to read data from that replica. $$t_{safe}$$ will be minimal one between the lastest commit timestamp and the timestamp it generates for a 2PC prepare step (if it has one). There are two situations based on how $$t$$ is chosen:

1. If the transaction only involves one partition, it is chosen to be the last commit time of the partition. And in order tot get the last commit time, it needs to ask the leader of this partition.

2. If the transaction involves multiple partition, it would be expensive and complex to get a last commit time. So using `TT.now().lastest` as $$t$$ can garantee it will read a version more recently than current time. And replica will not store $$t_{safe} \geq TT.now().lastest$$ since the lastest commit timestamp must be earlier than current, so it needs to ask the leader to confirm whether there are new writes.

## 5. How to Avoid the Weakness

From the last section, we find Spanner cannot avoid the communication between data centers. So why Spanner is still a big deal? We must look into real world problems. In real world, the weakness could be avoid if you understand it. Here is some suggestions when designing your application.

### 5.1 Partition Database Based on Location

A common pattern for globally distributed business is most transactions are happened only on the data in a limited location. For example, in the businesses like event booking, online ridesharing,  Groupon or some online market, the clients and the data are basically in the same city. So in these business, you can partition the data based on location, and put the leader of each partition on that location.

In this setting, most of the requests can be done in closed data centers while transactions that involve multiple data centers can also be done which guarantees external consistency.

Another interesting implementation detail about Spanner is it have three [kinds of replica](https://cloud.google.com/spanner/docs/replication): the read-write replica, read only replica and witness replica. Carefully plan these replicas based on the business and location can make most transactions operate on nearby data centers.


### 5.2 No Need to Always Avoid Stale Read

Serializable transactions make sure you cannot read old values. But in many cases, you may not need such guarantee. Spanner have an API to let the client specify a time interval, and let Spanner to guarantee it will not read older data that this time. For example, I can request some data no older than 10 seconds ago. When do we only need such a guarantee? I can give some examples here:

1. If you are using Twitter or Facebook, the count of likes doesn't need to be in real time. A delay around 10 seconds is acceptable. So when user request a Tweet's like count, he can read a snapshot on the past.
2. If you update your configurations in platforms like Google AdWords, the configuration need not take effect in real time. So when each device read the AdWords configuration, it can read on a past snapshot.
3. When generate a report, like how many user views for each webpage, the result need not to be in real time, either.

So what's the benefits to allow stale read? It turns out Spanner will sync up the data and safe time to all the replica. So if the time you specify is a relatively long interval, e.g. 10 seconds, you can read from a closest server and avoid all the communication between data centers, which will make the read fast.


## 6. Open Source Implementations

Google have released Spanner on Google Cloud, which makes it possible for everyone (with money (and not behind GFW)) to use. But if you don't want to stuck on one provider or interested in more implementation details, you may want to look into some open source ones.

In general, if we know the theory behind a software, we can hope there are some open source implementations. The quality of these open source software is only a matter of time and focus. However, in Spanner's case, a hardware clock is needed, which is rare for normal users. So the open source implementations are a little different from Spanner's original one. Let's take a look at two popular implementations and see how they get rid of the lack of hardware clock and how the implementation effect the result.

### 6.1 TiDB

TiDB uses a single time server to generate the transaction ID in order to make it monotone increasing. This is simple and they claim the throughput of the time server is very high and can be deployed in an HA way. But the down side is also very obvious: the latency would be high if you have data centers around the world. This doesn't only effect the read-write transactions, but will effect all the requests: include the read-only ones with snapshot isolation: since when doing readonly transaction, the client's timestamp is not trustable, it can only get a trustable timestamp from the time server. Of course client can cache some timestamps returned from the server and maintain them to use in the future requests, but it would be complex and error-prone. In fact, TiDB's developer recommends the network delay between the servers under 5ms in the section "What's the recommended solution for the deployment of three geo-distributed data centers?" of [their FAQ](https://github.com/pingcap/docs/blob/master/FAQ.md). Considering light still needs about 6ms to travel from Beijing to Guangzhou and needs about 12ms from New York to San Francisco, the requirements is not possible to reach in a real globally distributed cluster. And maintain a stable latency in a globally scale is hard and expensive, too.

Since the most highlight part of Spanner is globally distributed, the down side of TiDB is really a big deal. But the good part of TiDB is it compatible with MySQL protocol, which makes it to be a very easy solution if you want to scale out your old MySQL database.

And another different between TiDB and Spanner is TiDB only supports isolation level up to repeatable reads (or snapshot isolation), which is a weaker level than Spanner's serializable isolation.

### 6.2 CockroachDB

CockroachDB uses another approach to solve the lack of TrueTime API. The algorithm is a little complex. You can refer to [their blog](https://www.cockroachlabs.com/blog/living-without-atomic-clocks/) and the [HLC paper](https://cse.buffalo.edu/tech-reports/2014-04.pdf) for details.

The most obviously problem of this implementation is it lacks linearizability. To see what will happen when lacking linearizability, the "Comments" section of [Jepsen test for Cockroachdb](https://jepsen.io/analyses/cockroachdb-beta-20160829) explains it very clearly.

### 6.3 Conclusion

| Database      |    Special Hardware        |    Isolation Level    |   Consistency Level | Centralized Time Server    |
|---------------|--------------------------- |-----------------------|---------------------|----------------------------|
| Spanner       |     Yes                    |   Serializable        | Linearizable        |  No
| TiDB          |     No                     |   Repeatable Reads    | Linearizable        |  Yes
| Cockroachdb   |     No                     |   Serializable        | Causal Consistency  |  No
