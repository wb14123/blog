---
layout: post
title: Spanner and Open Source Implementations
tags: [database, technology, distributed system]
categories: Technical
index: ['/Computer Science/Distributed System']
---

When [Spanner paper](https://ai.google/research/pubs/pub39966) was published, the use of a synced clock to implement a globally distributed database attracted a lot of attention. After these years, Google has put Spanner on its cloud to make everyone be able to use it. And there are also some open source implementations of Spanner in these years. In this article, I'd like to write about how synced clock makes Spanner special, some notes on using it, and how others implement it without a special hardware clock.

## 1. Clock Is Not Trustable

Almost every computer has a clock on nowadays. A surprising fact is, the clock is not really accurate and normally we cannot even know the upper bound of the error. There are some reasons for this:

1. Normally the clock is synced with a remote server using NTP. But the network latency upper bound is unknown.
2. Even if the local clock is synced accurately once, the hardware in a normal computer makes it inaccurate when time passes. The error depends on the temperature and so on.

For more details, you can read the section "Unreliable Clocks" in Chapter 8 of the book [
Designing Data-Intensive Applications](http://shop.oreilly.com/product/0636920032175.do).

It is not a big deal in normal life to have an unreliable clock, but it is a big deal when your consistency algorithm relies on it. We can see the details in the next section.

## 2. What is TrueTime API?

In order to get rid of unreliable clocks, Google uses some special hardware like atomic clocks to make them reliable. The time which can get from each server is still inaccurate, but the error has a known upper bound. Specifically, these APIs are provided:

| Method | Returns |
|--------| --------|
| TT.now()     | A time interval: [earliest, latest]
| TT.after(t)  | true if `t` has definitely passed
| TT.before(t) | true if `t` has definitely not arrived

As we can see later, the error upper bound will affect the latency of each transaction. Google said they normally maintain it under 7ms and with an average value of 4ms in their Spanner paper.


## 3. What TrueTime API Gives Spanner?

What makes Spanner special is how it uses TrueTime API to do concurrency control. Spanner can guarantee external consistency, which is the strongest concurrency model. It means Spanner is both serializable and linearizable, which makes it very easy for applications to avoid concurrency bugs. Serializable isolation can be found in many databases, but usually, they are single leader databases, or have a centralized server. This makes it very slow if the database is globally distributed since the network latency between data centers is large and unstable. If every transaction needs to communicate with a centralized server, the transactions will be very slow and unstable.

Before understanding how Spanner can do this, let's first look at how serialization transactions can be implemented in a centralized way. The simplest way is to let a transaction grant a lock if it wants to read or write a row so that this row cannot be modified or read at the same time by other transactions. This is a relatively strong restraint, it will give us bad performance since every transaction excludes each other. We can have a better solution: for read only transactions, serializable isolation is the same as snapshot isolation. So we can implement snapshot isolation for read only transactions in order to have better performance.

Here is how snapshot isolation works: for every row of data, the database stores not only the data but also a version of it. When a transaction starts, it will only read data earlier than it. Normally we assign an id for each transaction and use it as the version. So here is the key: transaction id needs to be monotone increasing so that a later transaction can only read earlier data. Making a globally monotone increasing sequence is very straightforward for a single machine, but it is very hard in a distributed environment without a centralized server.

Ordering transactions is complex but can be done without a clock. For example, the vector clock algorithm can do this. But this will not guarantee the accurate order of transactions. For example, if T1 commits before T2 start and they are operating on different partitions, the database may order T2 before T1. An implementation of Spanner Cockroachdb uses algorithms like this. We will analyze the problem of it in Section 6.2.

**So what TrueTime API really gives Spanner is generating monotone increasing transaction IDs and guaranteeing external consistency ( or linearizability) at the same time**: if T1 commits before T2 starts, then T1's transaction ID is always smaller than T2's transaction ID. Spanner implements this by getting a timestamp from TrueTime API that absolutely passed the current time and waiting to commit until the current time absolutely passed the timestamp. In Spanner's paper, there is proof that this can guarantee external consistency:

$$ s_1 < t_{abs}(e_1^{commit}) $$ (commit wait)

$$ t_{abs}(e_1^{commit}) < t_{abs}(e_2^{start}) $$ (assumption)

$$ t_{abs}(e_2^{start}) \leq t_{abs}(e_2^{server}) $$ (causality)

$$ t_{abs}(e_2^{server}) \leq s_2 $$ (start)

$$ s_1 < s_2 $$ (transitivity)

There are also other parts of Spanner that use TrueTime API, for example, the Paxos implementation uses TrueTime API for leader lease. But it is just an implementation optimization that can be done in other ways, too.

## 4. The Myths of Spanner

There are some myths and hype for Spanner. In this section, I will highlight the weakness of Spanner. I call it "weakness" not because other databases are doing better, but because they are not as good as people may think. And they are not necessarily bad things. It depends on how you understand and use it. If you don't understand it correctly and think all the things are done perfectly and magically, you may run into some problems.

Many people think when using Spanner, reads can go to a replica closest to the client most of the time, so communication between data centers is avoided. But this is not true. In fact, **all the serializable transactions, which include read-write transactions and read only transactions that want to read the most recent data, must communicate with the partition leader.** The theory is in the Spanner paper and there is also an explicit description in [Spanner Cloud's document](https://cloud.google.com/spanner/docs/replication).

Let's first look at the read-write transaction. Since read-write transactions will write Paxos log, it is straightforward to understand it must involve the leader. And more, it must wait for the major of replicas to confirm the writings in order to make sure the write will not be lost when the leader fails.

Then let's look at the read-only transactions. In Spanner, every replica keeps a timestamp $$t_{safe}$$. And if a read-only wants to read the data at timestamp $$t$$ and if $$t \leq t_{safe}$$, it is safe to read data from that replica. $$t_{safe}$$ will be a minimal one between the latest commit timestamp and the timestamp it generates for a 2PC prepare step (if it has one). There are two situations based on how $$t$$ is chosen:

1. If the transaction only involves one partition, it is chosen to be the last commit time of the partition. And in order to get the last commit time, it needs to ask the leader of this partition.

2. If the transaction involves multiple partitions, it would be expensive and complex to get a last commit time. So using `TT.now().latest` as $$t$$ can guarantee it will read a version more recently than the current time. And replica will not store $$t_{safe} \geq TT.now().latest$$ since the latest commit timestamp must be earlier than the current one, it needs to ask the leader to confirm whether there are new writes.

## 5. How to Avoid the Weakness

From the last section, we find Spanner cannot avoid communication between data centers. So why Spanner is still a big deal? We must look into real world problems. In the real world, the weakness could be avoided if you understand it. Here are some suggestions when designing your application.

### 5.1 Partition Database Based on Location

A common pattern for globally distributed business is most transactions are only on the data in a limited location. For example, in the businesses like event booking, online ridesharing,  Groupon, or some online market, the clients and the data are basically in the same city. So in these businesses, you can partition the data based on location and put the leader of each partition on that location.

In this setting, most of the requests can be done in closed data centers while transactions that involve multiple data centers can also be done which guarantees external consistency.

Another interesting implementation detail about Spanner is it has three [kinds of replica](https://cloud.google.com/spanner/docs/replication): the read-write replica, read only replicas, and witness replica. Carefully planning these replicas based on the business and location can make most transactions operate on nearby data centers.


### 5.2 No Need to Always Avoid Stale Read

Serializable transactions make sure you cannot read old values. But in many cases, you may not need a such guarantee. Spanner has an API to let the client specify a time interval and lets Spanner guarantee it will not read older data than this time. For example, I can request some data no older than 10 seconds ago. When do we only need such a guarantee? I can give some examples here:

1. If you are using Twitter or Facebook, the count of likes doesn't need to be in real time. A latency of around 10 seconds is acceptable. So when the user requests a Tweet's like count, he can read a snapshot of the past.
2. If you update your configurations in platforms like Google AdWords, the configuration need not take effect in real time. So when each device read the AdWords configuration, it can read on a past snapshot.
3. When generating a report, like how many user views for each webpage, the result need not to be in real time, either.

So what are the benefits to allow stale reads? It turns out Spanner will sync up the data and save the time to all the replicas. So if the time you specify is a relatively long interval, e.g. 10 seconds, you can read from the closest server and avoid all the communication between data centers, which will make the read fast.


## 6. Open Source Implementations

Google has released Spanner on Google Cloud, which makes it possible for everyone (with money (and not behind GFW)) to use. But if you don't want to stick to one provider or are interested in more implementation details, you may want to look into some open source ones.

In general, if we know the theory behind the software, we can hope there are some open source implementations. The qualities of these open source software are only a matter of time and focus. However, in Spanner's case, a hardware clock is needed, which is rare for normal users. So the open source implementations are a little different from Spanner's original one. Let's take a look at two popular implementations and see how they get rid of the lack of hardware clock and how the implementation affects the result.

### 6.1 TiDB

TiDB uses a single time server to generate the transaction ID in order to make it monotone increasing. This is simple and they claim the throughput of the time server is very high and can be deployed in an HA way. But the downside is also very obvious: the latency would be high if you have data centers around the world. This doesn't only affect the read-write transactions but will also affect all the requests: including the read-only ones with snapshot isolation: since when doing readonly transactions, the client's timestamp is not trustable, it can only get a trustworthy timestamp from the time server. Of course, the client can cache some timestamps returned from the server and maintain them to use in future requests, but it would be complex and error-prone. In fact, TiDB's developers recommend the network latency between the servers under 5ms in the section "What's the recommended solution for the deployment of three geo-distributed data centers?" of [their FAQ](https://github.com/pingcap/docs/blob/master/FAQ.md). Considering light still needs about 6ms to travel from Beijing to Guangzhou and needs about 12ms from New York to San Francisco, the requirements are not possible to reach in a real globally distributed cluster. And maintain a stable latency on a global scale is hard and expensive, too.

Since the highlighted part of Spanner is globally distributed, the downside of TiDB is really a big deal. But the good part of TiDB is it compatible with MySQL protocol, which makes it a very easy solution if you want to scale out your old MySQL database.

And another difference between TiDB and Spanner is TiDB only supports isolation levels up to repeatable reads (or snapshot isolation), which is a weaker level than Spanner's serializable isolation.

### 6.2 CockroachDB

CockroachDB uses another approach to solve the lack of TrueTime API. The algorithm is a little complex. You can refer to [their blog](https://www.cockroachlabs.com/blog/living-without-atomic-clocks/) and the [HLC paper](https://cse.buffalo.edu/tech-reports/2014-04.pdf) for details.

The most obvious problem of this implementation is it lacks linearizability. To see what will happen when lacking linearizability, the "Comments" section of [Jepsen test for Cockroachdb](https://jepsen.io/analyses/cockroachdb-beta-20160829) explains it very clearly.

### 6.3 Conclusion

| Database      |    Special Hardware        |    Isolation Level    |   Consistency Level | Centralized Time Server    |
|---------------|--------------------------- |-----------------------|---------------------|----------------------------|
| Spanner       |     Yes                    |   Serializable        | Linearizable        |  No
| TiDB          |     No                     |   Repeatable Reads    | Linearizable        |  Yes
| Cockroachdb   |     No                     |   Serializable        | Causal Consistency  |  No
