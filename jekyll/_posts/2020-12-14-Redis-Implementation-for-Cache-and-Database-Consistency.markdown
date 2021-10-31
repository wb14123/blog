---
layout: post
title: Redis Implementation for Cache and Database Consistency
tags: [Redis, database, consistency, Jepsen, distributed system]
index: ['/Computer Science/Distributed System']
---

*This article belongs to a series of articles about caching. The code in this article can be found at [my Github repo](https://github.com/wb14123/redis_lease).*

1. *[Use TLA+ to Verify Cache Consistency](/2020-11-02-Use-TLA+-to-Verify-Cache-Consistency.html).*
2. *Redis Implementation for Cache and Database Consistency. (This one)*

In the [last article](/2020-11-02-Use-TLA+-to-Verify-Cache-Consistency.html), we introduced an algorithm (described in paper [Scaling Memcache at Facebook](https://pdos.csail.mit.edu/6.824/papers/memcache-fb.pdf)) that can do a better job to maintain the data consistency between cache and database. We also used TLA+ to model the algorithm and verified it. In this article, we are going to implement the algorithm in real world for Redis. The implementation is very simple and doesn't need to change Redis itself. It's implemented it by using Redis script. However, it's much harder to verify the correctness. In order to do it, I used [Jepsen](https://jepsen.io/) to test it. If you look at the language analysis for the Github repo, you can see most of them are tests. The Redis script implementation, which is written in Lua, is only 5.1% of the project.

## Algorithm Description

We've described the algorithm in the previous article and even write a TLA+ model for it. But just make it easier for the readers, I'll briefly describe the algorithm here again. Basically, whenever the client get a value from cache, it will be assigned a unique ID (lease) for the key. When the client writes back a new value, it needs to provide the  key's newest lease ID. And delete the key will also invalidate all its leases.

## Implementation

The implementation uses [Redis script](https://redis.io/commands/eval), which is written in Lua. It can implement multiple operations and make them atomic. In theory, this can also be done by client, but Redis script provides a consistent implementation across different clients and makes it easier to use. The algorithm is easy, so the implementation is also straight forward. The implementations are under [scripts directory of the repo](https://github.com/wb14123/redis_lease/tree/master/scripts). These scripts also works for Redis cluster (but I didn't use Jepsen to test it under cluster mode). Here is an example implementation for get:


```lua
local key = '{'..KEYS[1]..'}'
local token = ARGV[1]
local value = redis.call('get', key)
if not value then
    redis.replicate_commands()
    local lease_key = 'lease:'..key
    redis.call('set', lease_key, token)
    return {false, token}
else
    return {value, false}
end
```

After load the script, you can use it like this:

```
redis-cli evalsha <script_sha1> 1 <key> <uniq_id>
```

It will return `value, nil` if there is value for the key, or `nil, lease` if there is no value.

One optimization here is, if we have value for the key, we will not store the lease. That's because in our use case, if we can get the value, we will not get it from database and write back to the cache. This avoids a lot of memory overhead.

Another important decision I made is, when get the value, the client needs to provide a unique ID instead of let Redis provide one. This is because I cannot find a good way to generate unique ID in Redis cluster. In a single instance, it's easy: just use a key and inc the value each time. You can still generate unique IDs for different keys on a cluster, but it adds a lot of memory overhead. So I decided to let clients provide it. Luckily, it's not hard, basically every language has UUID implementation and that's good enough.

## Testing

It's easy to implement something, but very hard to make sure it's correct. We can use TLA+ to model the algorithm and explore the state space, or use mathematical method to proof the correctness in theory. But once we implement the algorithm, it's something different. We cannot make sure it's exactly the same as what we've proofed. That's why I find using Coq to implement, proof and generate real code is fantastic. But in this case, it's not implemented in Coq, so we must find some other way to test it. By testing, we still cannot make sure it's 100% correct, we can just explore as many situations as we can and make sure the system doesn't behaves in a way we don't expect.

The tool we use here is [Jepsen](https://jepsen.io/). It provides lots of tools to make it easy to test distributed systems. It can generate many concurrent requests, import different kinds of failures (host down, network partition, clock drift, and so on) to the system, record all the requests and responses, and check the history at the end.

Here is the test case I write: for each client, generate random read and write operations. For read operation, read from cache first, if the value is not found, read from database and write back to the data. For write operations, write to the database and delete the key from cache. Then after all the read and write operations, check whether the data in cache and database are the same. The test case is very simple, it implemented the way we would use the cache.

By providing different arguments to the test command, you can run the test case with raw Redis get/set/del operations, or use get/set/del operation implemented by the scripts. You can also import cache failure during the test.

If we run the test with raw Redis operations, we can find the test is failed. In the last article, we discussed that using plain get/set/del cache operations cannot guarantee cache consistency, so this is expected. If we run the test with our scripts, we can find the test passed. If we run the test with cache system failure, we can see the test failed, which is also expected from last article. The inconsistent because of cache failure can be resolved by clean up cache data after restart. But if the client is failed, it will have the same problem (I didn't write the test case for this because it's hard to test client failure in Jepsen), but it's not a very good idea to cleanup cache in this case. Because client fails all the time, cleanup cache will make operations slow. The best way might be to setup an expire time so the data can be consistent after the key is expired.

Even though all the test result is expected, it doesn't make sure the implementation is correct, since there are still many situation I didn't test, like Redis cluster, network partition, database failure and so on. So welcome to add new test cases and break the system!
