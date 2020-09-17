---
layout: post
title: Do Not Use Cache Unless You Really Need To
tags: [cache, database, postgresql, redis, memcache]
---

> Premature optimization is the root of all evil.
>
> -- Donald Knuth

There are many cache systems nowadays, such and Redis and Memcache. These systems are beautiful and easy to use because they focus on one thing: speed. And it becomes a common sense that if things are not fast enough, use a cache layer. But I'll argue, don't use cache unless you really need to. It adds another system in the data storage, and there is no good tool to maintain the data consistency between the data source and cache. It's so hard to get it right. There are many widely used patterns for caching that prove to be problematic. In the future article, I will go through these patterns and see what could make data inconsistent. But in this article, let's first look at if you really need a cache by comparing the performance of databases and caches.

For the database, we choose PostgreSQL because it's open source, widely used, well documented and well designed. For cache, we choose Redis based on similar reasons.
