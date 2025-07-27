---
layout: post
title: Single Primary Architecture with Process Pause and Network Delay
tags: [leader election, zookeeper, etcd, paxos, distributed system]
index: ['/Computer Science/Distributed System']
---

Single primary architecture is very common in distributed systems, since the critical operations can be handled by a single node at all time, to avoid race conditions. However, implement such an architecture can be harder and easier to go wrong than many people thought. In this article, we will explore what can go wrong with some widely used implementations of single primary architecture.


## Challenges in Distributed Systems

Before we start to talk about the implementations, let's review the challenges in distributed systems first, since they are critical in the discussion of this article.

First, network messages can take arbitrarily long to be delivered, or even lost. This is widely understood by most of the people, however, the following two challenges are often overlooked:

The second challenge is clock is not reliable. It's easy to understand wall clock sync is not reliable through network because of the first challenge above. But even if you have a wonderful way to sync the clock, the speed of clock on each machine can still vary for reasons like hardware temperature, voltage and so on.

The third challenge is the process running the code can be paused for arbitrary long as well. This can happen in case of GC like in JVM and Go. Even though there are better GC algorithms to avoid the pause, especially stop the world pause, that can still happen. Even more, even for platforms without GC, the process can still be starved from the operating system's scheduler, or from some more under layer pauses like vmotion in a virtual machine.

For the last two problems, it's easy to be overlooked because in practise, the pause or clock skew is usually small. However, there is really no guarantee of the pause time or how much the clock can be off. So when designing an algorithm, we must take that into account if you want to design a correct system instead of a best effort system. When the latter can be useful in many cases, many people don't know the implication.

## A Naive Implementation of Single Primary Architecture

If we only want a hard coded single primary in a distributed cluster, the problem would be easy to resolve. However, it creates a single point of the failure. In order to resolve the it, we usually fail over the primary to another node if anything happens to the primary. In order to do so, every node needs to reach to a consensus about who is the new primary. This is a complex problem but fortunately there are already algorithms like Paxos to resolve it. Unfortunately, those algorithms are also complex and hard to implement. So in turn, most of the everyday services use an external service for leader election.

The most common approach is to use a third party service as a lock service. So when doing leader election, each node just try to allocate a lock from the lock service and only one of them can be successful. This approach is talked in the paper [The Chubby lock service for loosely-coupled distributed systems](https://static.googleusercontent.com/media/research.google.com/en//archive/chubby-osdi06.pdf), then got popular with services like Zookeeper and etcd. Basically the lock service uses algorithms like Paxos to guarantee the cluster can reaches consensus, so when each node propose itself to be the leader, only one would successful to reach a consensus.

Here is a common way to use the service using Java and etcd:

```
lockService = ... // zookeeper, etcd or any other client

// lock.isValid will become false after 10 seconds, the lockService will also
// allow other nodes to acquire the lock after that.
lock = lockService.acquire(ttl = 10s)

// refresh the lock on a background thread
new Thread() {
  while (true) {
    lockService.refresh(lock, ttl = 10s)
    sleep(1s)
  }
}

// whenever primary needs to do some operation
if (lock.isValid) {
  doThings() // do things that primary needs to do
}
```

So in here, let's assume `lockService` is a black box remote service that is able to grant a distributed lock to only one node at a time. The code requests a lock with a ttl of 10 seconds, and refresh the lock every second in a background thread. We give a ttl for the lock to handle primary failover: when the primary node dies, it gives opportunity for other nodes to become the primary by acquiring a new lock after the old one expired. At last, when there is any operation that only primary can do, it's guarded by the check of lock.

It looks good at the first look, but combined with the challenges we mentioned in the last section, we will see the problems.

## Problem with Network Latency

First let's look at what will happen when considering network latency. We can consider all `lockService.*` is going through network. So for example, when acquiring the lock, the acquire can be successful but the response only delivered after 10 seconds, when the lock is already expired.

This problem can be resolved by setting proper timeout for network request. For example, if we set timeout of network request to be 1 second, the lock has at least 9 seconds until expired. With proper lock ttl and network timeout, it should resolve the problem of network delay.

Another problem with network latency is if `doSomething` is calling another service, because of the network latency, when the service received the request, the node that sent the request may not be the primary anymore. Again, you can include a timestamp in the request and check it from the other service.

Both of the timeouts touches the area of clock speed.

## Problem with Various Clock Speed

When using timeouts, we get another problem when processes on different nodes can run with various clock speed. For example, in an extreme scenario the client's clock can be so slow that it thinks the network is not timed out yet when the lock is already expired in the lock service. While this scenario can happen in theory, and we would like to avoid relying on it when designing an algorithm, most people find it acceptable because it's so rare in practise if we select a large enough difference between network timeout and lock TTL.


## Problem with Paused Process

However, when considering the possibility of pause, the problem is more obvious since it can happen more often.

Let's say the process paused after we check the lock `if (lock.isValid)`, then when it starts to `doThings` after the check, the lock may already expired. You can add more checks before `doThings`, but as long as it pauses before `doThings`, there is no guarantee the lock is still valid when calling `doTings`.

You can remediate it by set the lock TTL to be the longest possible time of pause. However, there is really no guarantee of the longest time of pause. The best you can do is to make the possibility of the pause time exceed TTL to be really small.

So in short, there is no guarantee `doThings` will be executed on a single primary node when there is a process can be paused for an arbitrary long.

In the following sections, we will see how we can operate with the mindset of single primary is not guaranteed.

## Use External Service with Lock Lease ID

So if there is no guarantee there is a single primary at the same time, we need some external services to guarantee it. One of the methods is to let the external service to check the lease ID of the lock. This is actually a method talked in the Chubby paper above.

Lots of the lock service including Zookeeper, etcd has the ability to generate a monotonic ID for the acquired lock. For example, if the primary writes a database in `doSomething`, it can do this in a **transaction**:

```
if (sql("select * from lock where id == $1", lock.id)) {
  sqlUpdate() // do necessary updates in the same transaction
}
```

The key above is the check and update should be in the same transaction, so that there is no race condition when there is a new lock updated the ID. So using this approach basically offload the consistency guarantee to another part of the system, either be a database that has transaction support, or just a single node like a file server mentioned the paper of Chubby.

But such an external guarantee may not always be available. We will explore 2 more scenarios and the ways to resolve the problems when a single primary node cannot be guaranteed.

## Quorum Replication

What if there is no external service to guarantee `doSomething` will only be accepted for one node? For example, if we are writing a distributed database, how do save data when a single primary is not guaranteed? Write to a quorum is one of the solutions.

Here is how it works: primary node accept write requests and need to replica it to a majority of nodes to consider the write to be successful. Replica only accept the writes from the current primary (in its view).

The primary replicas the writes as logs. Each write has an ID keeps increasing by 1. So in case of 2 primary tries to replica with same write ID, the replica can detect the conflict and reject the later one.

So what happens if there are 2 nodes think they are primary at the same time? Only one of them will be able to write to a majority of the nodes. It can be the new primary or be the old primary. Either way, who wins the majority is the primary at the time. The node that failed to write the majority can give up the primary role and see if there is a new round of leader election needed.

A detail to notice in such scenario is, when doing leader election, only the node that can connect to a majority should request to be the primary. And during this, the node should negotiate what's the committed writes. They should only consider the writes to be committed if a majority of the nodes have it, and abandon any newer ones.

**TODO: how to check a node has a log that matches the majority?**. There can be a race condition: A checks itself matches majority of nodes, then got paused. At the mean time, C also checks and become leader, then lost leader somehow. Then, A resumed and requested to be the primary and successful. Then A thinks it has the write log and force sync it to other nodes which results transaction lost during the time period when C was primary. This seems can be avoid with an epoch number and the lock service only grants lease when epoch number is the largest it has seen. Can the following rule do it?


* Use zookeeper/etcd to elect a primary in the cluster.
* Primary replicate write logs to other nodes. When a primary replicate logs, it uses the current lease ID as epoch.
* Log is ordered with monotonic numbers and there is no hole in it.
* Replicas only accept replication command with largest epoch it has ever seen.
* Only consider writes to be committed when replicated to a quorum, and apply (save) the committed transaction on primary before response to client.
* When read, confirm itself is still the primary from zookeeper/etcd with linearizable op before get and return the data.
* If a node find out the primary is lost from zookeeper/etcd, it starts to do leader election by:
	* A node asks other nodes whether they have any newer log than itself. It needs to collect a majority of yes.
	* A node only replies yes to the ask if it doesn't have newer log, AND if it doesn't see any primary from zookeeper/etcd.
	* After a node replies yes, it only accept any replication command with larger epoch.
  * New leader can force sync logs to other nodes, which means if other nodes has conflicted logs, it will be resynced from new primary (only accept largest epoch it has ever seen).
  * Block queries until logs are replicated to a quorum.

Here is an example of the events of such an scenario (P means the current primary):

| Time | Event                                                                                                                                                            | A                        | B                        | C                        | D                        | E                        |
| ---- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------ | ------------------------ | ------------------------ | ------------------------ | ------------------------ |
| 1    | Init state                                                                                                                                                       | P: A<br>Data: []         | P: A<br>Data: []         | P: A<br>Data: []         | P: A<br>Data: []         | P: A<br>Data: []         |
| 2    | New write request to set k1=v1                                                                                                                                   | P: A<br>Data: [1: k1=v1] | P: A<br>Data: []         | P: A<br>Data: []         | P: A<br>Data: []         | P: A<br>Data: []         |
| 3    | Network partition happens, the clusters is divided to (A,B) and (C,D,E). C is elected as new primary.                                                            | P: A<br>Data: [1: k1=v1] | P: A<br>Data: []         | P: C<br>Data: []         | P: C<br>Data: []         | P: C<br>Data: []         |
| 4    | New write request to set k1=v2                                                                                                                                   | P: A<br>Data: [1: k1=v1] | P: A<br>Data: []         | P: C<br>Data: [1: k1=v2] | P: C<br>Data: []         | P: C<br>Data: []         |
| 5    | Both write requests get replicated, k1=v1 is not considered committed since it's not replicated to majority                                                       | P: A<br>Data: [1: k1=v1] | P: A<br>Data: [1: k1=v1] | P: C<br>Data: [1: k1=v2] | P: C<br>Data: [1: k1=v2] | P: C<br>Data: [1: k1=v2] |
| 6    | Network partition recovered, A and B found the transaction 1 is in conflict with the majority of nodes, so they abandon it and sync the history from other nodes | P: C<br>Data: [1: k1=v2] | P: C<br>Data: [1: k1=v2] | P: C<br>Data: [1: k1=v2] | P: C<br>Data: [1: k1=v2] | P: C<br>Data: [1: k1=v2] |

So in the events above, even though A and C both think itself is the primary for a while, only 1 can win the primary. And only the data is written to a majority of nodes is considered to be committed.

## Quorum Reads and Linearizability

If there is a read request between step 5 and 6 and if it happens to fall onto node A, since node A still thinks it's the primary, it will happily return nothing since `k1=v1` is not considered committed. However, another transaction at step 5, which is strictly earlier than this read request, already knows `k1=v2` is committed. This is a violation of [linearizability](https://en.wikipedia.org/wiki/Linearizability). Which in practice can be confusing: thinking about a social network use case, the user may just posted something and found it disappeared in a following request.

This can be resolved by checking the primary role before returning the read data. Note the primary role can be expired even after the checking, but that means the read request is at least started before the node is still primary, which makes the read and write request to be in parallel instead of having a strict order.

## PostgreSQL Leader Election
