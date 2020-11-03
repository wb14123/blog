---
layout: post
title: Use TLA+ to Verify Cache Consistency
tags: [TLA+, cache, database, consistency, algorithm, distributed system]
---

> There are only two hard things in Computer Science: cache invalidation and naming things.
>
> -- Phil Karlton

During web service development, it's very usual to use a cache before the database. It's so common that almost becomes the default solution whenever there is a performance issue. But a lot of people don't really think about the consistency between database and cache. A main reason is it's so hard to reason about the consistency under a distributed system. So in this article, I will explore how to use TLA+ to specify different cache algorithms and use TLC to check whether it will keep the data consistency between cache and database. All the code in this article is available at [my Github repo](https://github.com/wb14123/tla-cache). The code may be updated after this article is published.

## System Architecture

Let's first describe the normal architecture of the cache and database system. Normally, a web service query data from a database and save data to it. The service itself usually doesn't store any stateful data. This makes it's very easy to scale up: just start a bunch of servers and put them behind a load balancer. But for the database, it's not so easy. It's usually very hard to scale up a database. So when the performance of the database is an issue, we usually put a cache before it. The cache stores everything in memory so it would be much faster and can handle much more requests than a traditional database that needs to persistent everything. When we read data, we read cache first and only load it from database if there is no data in cache. When save data, we must persistent the data to database. Here is a graph about what this architecture looks like:

```
web_server_1
web_server_2
web_server_3  <--> cache <--> database
...
web_server_n
```

We need to notice this is a different architecture than the cache of CPU. In multi-core CPUs, each core has it's own cache instead of having a shared cache, which looks like this:

```
core_1 <--> cache_1
core_2 <--> cache_2
core_3 <--> cache_3  <---> main memory
...
core_n <--> cache_n
```

Because of the differences of architectures, consistency and latency requirements, they usually needs different solutions. In this article, we only talk about the cache algorithms for web services.

## Cache Algorithms

The cache algorithms are very simple. When read data, read data from cache first. If there is no data in cache, read from database and write it back to cache. When write data, because normally database has stronger consistency model and can persistent data better, we usually write to database first, then write to cache or invalidate cache. In our example, we invalidate data after write since it's the most widely used one and has less consistency issues.

This is the pseudocode for this algorithm:

```
read(key) {
    cache = readCache(key)
    if (cache != null) {
        return cache
    }
    data = readDB(key)
    writeCache(key, data)
    return data
}

write(key, value) {
    writeDB(key, value)
    invalidateCache(key)
}
```

## TLA+ Specification

Once we have the algorithm in mind, we can write a TLA+ specification and let TLC to check whether it has the properties we want. A TLA+ specification is not a 100% map from the system, it's an abstraction that omits irrelevant details. In our specification, we make two key abstractions:

1. Data is inconsistent between cache and database if one row is inconsistent. So in the specification, we only care about one row. Which means we don't need the parameter for `key`.
2. In the specification, we don't care about what's the actual value as long as each client writes different values. So we let the client write it's own ID as the value. Thus we can also omit `value` from write behavior.

We need also to notice that if we write multiple state changes in one TLA+ statement, it means those state changes are atomic. In the code, we assume read/write cache/database is atomic while others are not, which means each line is an atomic operation but the lines between them are not. So for each of the lines, we should write separate statements.

So keeping this in mind, we have these variables for our specification:

```
CONSTANT CLIENTS
VARIABLE cache, db, cacheResults, dbResults, cacheWritten, states
```

* CLIENTS: all the clients
* cache: the value in cache
* db: the value in database
* cacheResults: read cache results for each client
* dbResults: read DB results for each client
* cacheWritten: whether the cache has been written.
* states: the state of clients

The reason we want to have a variable `cacheWritten` is, we want to make sure the algorithm really wrote to the cache. Otherwise it would be simple to keep data consistent by not using the cache at all. (We don't check this property in this article, but it's not hard to add that).

For `states`, we want all client start with `free` and ends with `done` when read/write is done. It can go from `done` to `free` again to start another read/write.

Then we have `Null` value for no data in cache or database, `InitValue` for any value that exists before the system is running, and all the data that could be put into cache and database;

```
Null == "null"
InitValue == "init"
Data == CLIENTS \union {Null, InitValue}
```

Once these basic values are ready, it's not hard to write the specification for cache and database interface. The specification of it is in [CacheInterface.tla](https://github.com/wb14123/tla-cache/blob/master/CacheInterface.tla). Then we can use the interface to specify the algorithm described above: [WriteInvalidateCache.tla](https://github.com/wb14123/tla-cache/blob/master/WriteInvalidateCache.tla).

Finally, we want to also specify what property we want for our system. We want the data in cache and database to be consistent. It would be very hard to make them to be the same all the time. So we make a reasonable weaker statement: we want to make sure once all the clients are done, either the cache doesn't have any data, or it has the same data as in database:

```
AllDone == \A c \in CLIENTS: states[c] = "done"
Consistency == IF AllDone THEN (cache = db \/ cache = Null) ELSE TRUE
```

We can put the specification and all the properties we want to check into a TLC model config file [WriteInvalidateCache.cfg](https://github.com/wb14123/tla-cache/blob/master/WriteInvalidateCache.cfg) and let TLC to check it:

```
tlc WriteInvalidateCache
```

After running this, it will show it violates `Consistency`, and also show all the sequences to reach the violation. In this way, we know this algorithm cannot guarantee consistency between cache and database.

## A Better Algorithm

An algorithm that makes data possible to be inconsistent doesn't necessary makes it a bad algorithm. It maybe faster than stronger consistent algorithms and some application is fine with stale data. It all depends on the use case. What makes it a bad algorithm is people use it without truly understand it.

In this section, I'll introduce an algorithm with better consistency. It's introduced by the paper [Scaling Memcache at Facebook](https://pdos.csail.mit.edu/6.824/papers/memcache-fb.pdf). In this algorithm, if there is a cache miss during read, the cache server will return a lease token to client. A newer token or the invalidate of the cache will make previous token invalidate. The client can only write cache if it has a valid token.

I implemented the specification of this algorithm in [WriteInvalidateLease.tla](https://github.com/wb14123/tla-cache/blob/master/WriteInvalidateLease.tla) and the model config [WriteInvalidateLease.cfg](https://github.com/wb14123/tla-cache/blob/master/WriteInvalidateLease.cfg). If you run the TLC checker on it:

```
tlc WriteInvalidateLease
```

You will find it passed the check. Because we specify two clients in `WriteInvalidateLease.cfg`, so it means it meets the `Consistency` requirement with two concurrent clients:

```
CLIENTS = {"c1", "c2"}
```

You can make it as many clients as you want. But increase one client will increase the checking time a lot. Usually 3 clients is enough. It will not guarantee it meets the property under unlimited clients (we need formal proof to do that), but it will give us much higher confidence on the algorithm.

## What Else Can Go Wrong

In the algorithm above, we can see the data in cache and database can be consistent once the client has done the work. But it doesn't say any thing about client failure. Because the client write to database then invalid the cache, if the client is down during these two operations, it will not invalid the cache so it will have stale data. To specify this in TLA+, we can add two behaviors into Next:

```
Failure(c) == /\ states' = [states EXCEPT ![c] = "fail"]
              /\ UNCHANGED <<cache, db, cacheResults, dbResults, cacheWritten, lease>>

Recover(c) == /\ states[c] = "fail"
              /\ states' = [states EXCEPT ![c] = "start"]
              /\ UNCHANGED <<cache, db, cacheResults, dbResults, cacheWritten, lease>>
```

Since it's much less likely for the server to not graceful shutdown, this problem is much smaller than the previous one. And the effect can be limited by having a TTL for cache. However, you need to understand that to know if the algorithm really meets your need.

Another thing need to notice is the consistency property we defined above is a really weak one. It doesn't guarantee you always read the newest data.

## Conclusion

In this article, we can see how easy a cache algorithm can go wrong and how to use TLA+ to specify and verify it. Based on the specification examples above, you can verify any cache algorithm you like: for example, instead of invalidate the cache after write to database, update the cache and see how it works.

In next articles, I will write an implementation for Redis to use the lease token algorithm. And compare the performance between database and cache to see if we really need to use cache in some use cases.
