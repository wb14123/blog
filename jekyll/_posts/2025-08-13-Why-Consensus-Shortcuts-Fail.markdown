---
layout: post
title: "The Danger of Half-Implementing Raft: Why Consensus Shortcuts Fail"
tags: ["distributed system", "raft", "paxos", "etcd", "postgresql"]
index: ['/Computer Science/Distributed System']
---

There are some battle-tested consensus algorithms like Paxos and Raft to ensure a consistent view will be reached in a distributed system, even with scenarios like node failure, network partition, clock skew, and so on. There are existing systems like etcd and Zookeeper that implement these algorithms so you can use them as external systems. But sometimes, you may need to embed the algorithm into your own system instead of relying on a third-party one (it may be more common than you think—more on that in my next blog). Sometimes, because of the limitations of existing systems or other reasons, it may be hard to fully implement the algorithm, so people may tend to cut some corners and think it will be okay. While Raft and Paxos are not the only consensus algorithms, creating new ones is error-prone. This article explores how partial implementations fail, even when leveraging existing Raft systems incorrectly. We will use Raft as an example instead of other algorithms like Paxos, since it's easier to understand and has a better description of real-world systems.

## Goal of the System

Before going to the details, let's define what the goal of the system or algorithm we are going to implement is, because a consensus system can mean many things. 

I'll quote from Raft's paper [In Search of an Understandable Consensus Algorithm](https://raft.github.io/raft.pdf) for the properties of the system:

> Consensus algorithms for practical systems typically have the following properties:
>
> * They ensure safety (never returning an incorrect result) under all non-Byzantine conditions, including network delays, partitions, and packet loss, duplication, and reordering.
> * They are fully functional (available) as long as any majority of the servers are operational and can communicate with each other and with clients. Thus, a typical cluster of five servers can tolerate the failure of any two servers. Servers are assumed to fail by stopping; they may later recover from state on stable storage and rejoin the cluster.
> * They do not depend on timing to ensure the consistency of the logs: faulty clocks and extreme message delays can, at worst, cause availability problems.
> * In the common case, a command can complete as soon as a majority of the cluster has responded to a single round of remote procedure calls; a minority of slow servers need not impact overall system performance.

To be clearer, by "ensure safety", it means to guarantee [linearizability](https://jepsen.io/consistency/models/linearizable). And by non-Byzantine conditions, I'll quote from the Paxos paper *Paxos Made Simple*:

> We use the customary asynchronous, non-Byzantine model, in which:
>
> * Agents operate at arbitrary speed, may fail by stopping, and may restart. Since all agents may fail after a value is chosen and then restart, a solution is impossible unless some information can be remembered by an agent that has failed and restarted.
> * Messages can take arbitrarily long to be delivered, can be duplicated, and can be lost, but they are not corrupted.

When we explore what happens if we don't fully implement Raft, we will refer to these failure modes very often since that's when interesting things happen.

## What Does the System Do

Paxos only discusses the consensus of a single value in a distributed system. But as the Raft paper noted, it's not very practical in real-world systems. There are algorithms that leverage Paxos, like Multi-Paxos, but they are hard to understand. So here we will follow Raft's direction to describe a system that guarantees each node has a consistent view of logs. The logs are like write-ahead logs of databases like PostgreSQL and MySQL. Once each node has them and they are guaranteed to be consistent, they can just apply the logs to have a consistent state.

Raft elects a leader to answer all client requests to simplify the algorithm. Log replication is from the leader to other nodes.

So in the following section, we will discuss how it will affect the consistency of logs across different nodes if we miss some parts of the Raft algorithm. We will use PostgreSQL as an example when discussing it, since its write-ahead log (WAL) is like Raft logs but missing some information. We will see how this difference will make it hard to implement a linearizable, highly available PostgreSQL cluster.


## No Guarantee of A Single Leader

An important thing to notice before we go further is that there is no guarantee that only a single node is the leader at any given time. More specifically, there is no guarantee some operations are only done by a single node at a given time. Let's consider the following code for operations the leader does:

```
if (is_leader()) {
  do_something()
}
```

As described above for the non-Byzantine model, a process may pause for arbitrary long. So if the process is paused after `is_leader()` returns true, in order for the system to still be available, another leader should be elected. But when the old leader resumes and comes back, it thinks it's still the leader since from its perspective, it just checked that. You can add more checks before `do_something()`, like checking the time passed since `is_leader()` and so on, but the pause can always happen after all the checks and just before `do_something()`.

Not only can process pauses cause issues. If you are doing any network requests in `do_something`, because of network latency, when the requests reach to the other node, a new node may be elected as a leader by then.

This is not only something that can happen in theory. Some languages have built-in GC that are known to have long GC pauses under some scenarios. And the operating system can also be too busy to schedule the process back to the CPU. If the process is running in a virtual machine, the host can also pause the virtual machine. To try to resolve it, some systems delay the new leader election when the old one loses contact, in the hope that the process pause or the network delay would be over by then. However, there is really no guarantee of the length of a process pause. So the delay of leader election only makes the possibility smaller, but not zero.


## A Naive Implementation with Existing Raft System

The most naive implementation may be just leveraging an existing consensus system like etcd, which implements the Raft algorithm. However, with this approach, we get the same problem as described in the last section. Let's say you use etcd to do an `is_leader` check every time you want to do something that only the leader can do: the process can pause after the check, so there is really no guarantee of a single leader, even when leveraging an existing system that implemented the Raft algorithm.

## Leader Terms

Raft and similar systems resolve the problem by having a leader term for each new election. The leader terms are consecutive integers. And when the leader sends a request to replicate logs, it sends its current term with the request. On the other node, it can compare the highest term it knows. If the term it knows is higher than the one in the request, it means the request is sent by an old leader, thus it will reject the request.

In terms of using an external system like etcd as described in the last section, we can use the leader term to guard operations. In etcd, there are revisions for a key, which is basically the same as leader terms. For example, if `do_something()` above is updating some rows in a database, you can check if the current term is the same as the term in the database. Here is a code example:

```
key = get_etcd_leader_key()
if (key != null) {
  revision = key.revision
  begin_db_transaction()
  if (get_revision_in_db() <= revision) {
    do_something();
    update_revision_in_db(revision);
  }
  end_db_transaction();
}
```

Note how `get_revision_in_db`, `do_something` and `update_revision_in_db` happen in the same database transaction. Essentially, we are using another consistent system, the database, combined with the leader terms to make sure there is a single leader doing the operation.

## High Available PostgreSQL Cluster

But what if we are implementing a distributed database system? There is no external database for us to depend on. For example, let's say we are implementing a highly available cluster for PostgreSQL. Is there a way to use an external Raft system like etcd to implement a PostgreSQL cluster so that it meets the properties we discussed in the section "Goal of the System"?

PostgreSQL has tools to create replication and guarantee the transaction is only considered committed when it already replicates to a specific number of replicas. (Even though the implementation also has problems, but we will ignore them for now. See the section "A Known Issue of PostgreSQL Replication" in [my previous article](/2024-12-02-PostgreSQL-High-Availability-Solutions-Part-1.html) for details). But it doesn't have an auto-failover mechanism. So one needs to implement that part, and it's actually very tricky.

One may think that with an existing Raft system like etcd, each node can have a process to monitor if it's still the leader. And the nodes can reset the replication configuration based on who is the leader told by etcd.

However, since we've already seen from the sections above, we cannot simply just check who is the current leader. Otherwise, two nodes may very well think they are both the leader at the same time. So how should we resolve the problem?

## Quorum

A way to reach consensus is to write a log entry to a majority of the nodes. Let's say the log has an index along with its content, where the index is the position of the log and they are consecutive integers (no holes in the log entries). When a primary wants to replicate a log entry at index N, the replicas only accept it if index N is their next local index entry. With this, no matter who is the primary, only one log entry will be persisted on a majority of nodes for the same index N. And when we read data, we can also read from a quorum to make sure the data is on a majority of nodes when we return it to the client.

However, it has problems when we think about more than one log entry. Consider the following timeline of events:

1. We have 5 nodes A, B, C, D, E. Both A and C think they are the leader.
2. A tries to replicate (index=1, content=X). At the same time, C tries to replicate (index=1, content=Y).
3. A wins A, B while C wins C, D, E. So the current state is A, B has [X] while C, D, E has [Y]. But since the majority of nodes have [Y], we can ignore the version history of [X].
4. A then tries to replicate the next log entry (index=2, content=X) even though it doesn't get a majority from the last replication (more on this later). C tries to replicate the next log entry too with (index=2, content=Y).
5. This time A wins a majority with A, B, D while C wins C, E. So the state becomes:
  * A: [X, X]
  * B: [X, X]
  * C: [Y, Y]
  * D: [Y, X]
  * E: [Y, Y]

We can see at step 5, there is no log history that has a majority.

You may have some questions about step 2: why does A try to replicate the next log entry even though it doesn't get acked by a majority for the previous one? This is a normal performance optimization. Otherwise, each transaction needs to wait for the previous one to finish, which is very slow.

But let's say for the sake of correctness, a node only replicates the next log entry when a majority acked its previous replications, problems can still happen:

1. We have 5 nodes A, B, C, D, E. Both A and C think they are the leader.
2. A tries to replicate (index=1, content=X). At the same time, C tries to replicate (index=1, content=Y).
3. A wins A, B while C wins C, D, E. So the current state is A, B has [X] while C, D, E has [Y]. But since the majority of nodes have [Y], we can ignore the version history of [X].
4. However, let's say C then starts to replicate (index=2, content=Z). A, B have no reason to reject it since index 2 is after their latest index 1. So A, B has [X, Z] while C, D, E has [Y, Z].

From a log entries point of view, we still maintain a log history within a majority of the nodes, which is [Y, Z]. But the problem is we need to apply the logs to state. When this happens, the internal state of A, B is different from the internal state of [Y, Z]. When we say check majority when reading data, we cannot check the whole log history of each node or the whole state of each node, otherwise the performance would be very bad.

But let's ignore the performance again and compare the whole log history or state when reading—problems can still happen during a failover:

1. We have 5 nodes A, B, C, D, E. Both A and C think they are the leader.
2. A tries to replicate (index=1, content=X). At the same time, C tries to replicate (index=1, content=Y).
3. A wins A, B while C wins C, D, E. So the current state is A, B has [X] while C, D, E has [Y]. But since the majority of nodes have [Y], we can ignore the version history of [X].
4. Node C failed and a new election needs to happen. The current state is:
  * A: [X]
  * B: [X]
  * C: failed so unknown.
  * D: [Y]
  * E: [Y]

So none of the log histories has a majority. It's impossible to know which one to pick in this scenario.

## Save the Last Log Entry to External Raft System

What if we save the last log entry, including the log index and (the hash of) its content, to an external Raft system like etcd, and only consider it to be committed if it can successfully do so? So that when a new leader needs to be elected, it checks its eligibility by checking whether it has the latest committed log entry saved in etcd. Here are some more details to guarantee the correctness:

* Only the leader can accept queries. It replicates log entries to a majority of nodes, then saves it to etcd. The log entry is only considered committed if those two operations succeed.
* When saving the latest log entry to etcd, it needs to check it's still the leader in the same transaction, so that an older save request wouldn't override a newer one.
* The leader should also tell the replicas the log entry is committed, so that the replicas can check with etcd and also commit it locally. If the entry in etcd is already larger than the latest one locally, it should ask the leader to sync the log entries.
* When a replica receives a replication request, it only appends it to its committed local entries. And the index of the new entry should be just behind its committed last entry. If the new index number is lower, it rejects it. If the new index number is higher than 1, it needs to ask the leader to sync its log entries.
* When a node wants to become a leader, it needs to check it has the latest log entry in etcd and acquire the leader key in the same etcd transaction.
* When reading data, it needs to confirm with etcd that the node has the latest log entry locally.
* The new leader should try to force sync its logs to other replicas to resolve conflicts. The replica needs to guard the sync request with the leader term: only accept requests with leader terms larger than the one stored locally. Otherwise, an old sync request may override newer committed log entries, which makes the committed log entries not replicated to a majority of nodes.

You can see the algorithm is already complex. The complexity is even comparable to Raft, if not more so. But even with this, I don't have proof it is correct. But at least I cannot find any problem for now. However, this approach comes with a performance penalty: every log entry is essentially replicated to 2 systems: one for the system itself and one for the external Raft system. It will double the latency.

For systems like PostgreSQL, you cannot force it to replicate the write-ahead logs one by one without modifying it. It's also hard to guard the replication with a committed check with etcd. So it's really hard to implement this for a highly available PostgreSQL cluster, even not considering the performance issues.

## Leader Terms in Logs

The Raft algorithm resolves the problems by also including leader terms in the log entry and having guards around that when replicating log entries and when failing over. The paper already talked about it in detail, so I'll not repeat it again here. The point is, without the leader terms in the log structure, it's really tricky to make such a consensus system. We'll talk more about it in the last section.

## Linearizable Guarantee for Reads

One thing that can be overlooked is the leader's behavior for guaranteeing linearizable reads. Maybe because it's discussed in a later section (Client interaction) in the Raft paper. There are 2 problems about the leader having stale data:

First, the Raft algorithm sends the last committed log ID after replicating the log entries in separate requests. So when a new leader is elected, it may have log entries that are already considered committed but are not known to the new leader yet. So the new leader needs to do a no-op log entry replication. During such replication, based on the Raft algorithm's logic, the last committed log ID will be synced and caught up.

Then, when reading data from a Raft system, a node may think it's still the leader but in fact there is a newer leader already elected. So before processing the read request, it needs to confirm it's still the leader. The Raft algorithm does this by checking heartbeats between the leader and replicas.

Even etcd didn't implement the linearizable reads at first. You can check more details from [this Jepsen test](https://aphyr.com/posts/316-jepsen-etcd-and-consul).

Using PostgreSQL as an example again, let's say we somehow make the log replications to be consistent across the nodes, but we still need to check the leader's role on every read. This is not possible without having a layer before the actual PostgreSQL transaction, either through PostgreSQL extensions or through a proxy.

## Conclusion

We talked about some different implementations for a consensus system and how things can go wrong with those implementations. We can see without an overall consideration, how hard it can be to implement such a system. Using PostgreSQL as an example, even though it has a built-in replication mechanism, without an overall design considering distributed consistency and failover, it's hard to implement a linearizable system without changes to its internals.

But a linearizable system may not always be needed. Sometimes we may want a less strict model, like only guaranteeing read-your-own-writes. But even with this less strict requirement, it still needs significant considerations. Maybe that can be a topic for future discussion.
