---
layout: post
title: Use Redis to Count Statistics Instead Of Spark Streaming
tags: [big data, redis, spark]
---

In my work, I need to count basic statistics of streaming data, such as mean, variance, sum and so on. The streaming data belongs to several groups, I need to count statistics for each of them. At first, I'm using Spark Streaming, but after a while, I reimplement it with Redis and find it is much better. I'd like to talk about them in this article.

The Problem
-------------

Let's talk about the problem first. I've simplified it: assume we have a book store and we'd like to know which kind of book sells best. When a custom comes and buy a book, we can collect data like this: (`category`, `book`, `price`). Then we'd like to get the mean, variance and sum of the money by book for each category. For example, if we have a data stream like this:

```
(c1, b1, 10)
(c1, b2, 30)
(c2, b3, 10)
(c1, b1, 5)
```

Then the mean of category `c1` is `[(15 + 5) + 30] / 2 = 22.5`.

Analyzing the Problem
--------------

Sum and mean is very easy to compute, the tricky one is to compute the variance. We will use this formula:

<span>$$ var(\vec{a}) = {mean(\vec{a})^2} - {\sum_{i=1}^n a_i^2 \over |\vec{a}|} $$</span>

So we need to count the sum of squares: <span>$$ \sum_{i=1}^n a_i^2 $$</span>.

Since <span>$$ a_i $$</span> is changing as the data is coming, we need to keep tracking all the elements in <span>$$ \vec{a} $$</span>. This is the key of the problem.

Next, I'll show you how to compute sum of squares with Spark Streaming and Redis.


Using Spark Streaming
----------------

```

def updateFunc(newValues: Seq[Double], runningCount: Option[Double]): Option[Double] = {
}

val sumOfSquares = sc.updateStateByKey[Double](updateFunc _).map(a => a * a)

```

The code is clean and easy to read. But there are two problems, both are about `updateStateByKey`:

1. `updateStateByKey` has a state in memory, we need to enable checkpointing for it in order to support fault tolerance. And in the case of updating the code, we need to save the state by ourself, as I have specified in [an earlier blog](/2015-11-03-the-proper-way-to-use-spark-checkpoint.html). When the state is big, it will be very slow and complex.

2. `updateStateByKey` is not so fast. It will run against all the elements in it every time. It is not necessary in our situation. The Spark guys seems to [realize this problem](http://technicaltidbit.blogspot.sg/2015/11/spark-streaming-16-stop-using.html), too.

I tried this program with billions of elements and save the state data into Cassandra. I ran this on a cluster with 4 machines, each of them have 32GB RAM. The program simply cannot save the state data into Cassandra and crashed.

So what do we need? What we really need is just a place to record and update the vector <span>$$\vec{a}$$</span>. Redis is the perfect tool to do this thing.

Using Redis
----------------

```
val redis = RedisClient()
val resultKey = "sum_of_squares"

stream.foreach { elem =>
  val newValue = redis.incrbyfloat(elem.i, elem.v).getOrElse(elem.v)
  val oldValue = newValue - elem.v
  val incValue = (newValue * newValue - oldValue * oldValue)
  redis.incrbyfloat(resultKey, incValue)
}
```

Comparing to the Spark version, the compute part has been turned into a single thread program (Redis is a single thread program). But this is a real time data stream, CPU should not be the bottleneck before IO. And this program uses no more memory than the Spark version. If the memory doesn't fit into a single machine, we can use [Codis](https://github.com/wandoulabs/codis) (or other Redis Cluster solutions).

This program can compute billions of elements in one day. And the memory usage of Redis is about 100GB for billions of keys. (The raw problem I'm solving is more complex than I described here so I'm storing much more data than this problem needs.)
