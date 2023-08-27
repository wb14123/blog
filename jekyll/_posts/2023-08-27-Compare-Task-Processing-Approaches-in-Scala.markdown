---
layout: post
title: Compare Task Processing Approaches in Scala
tags: [Scala, concurrent, cats, cats-effect, fs2, queue, stream]
index: ['/Computer Science/Programming Language/Scala']
---

*All the source code mentioned in this blog can be found in [my Github repo](https://github.com/wb14123/scala-stream-demo).*

## Task Processing

There is a common problem in computer science and I've met it again recently: how to generate and process tasks efficiently? Use my recent project [RSS Brain](https://www.rssbrain.com) as an example: it needs to find the RSS feeds that haven't been updated for a while in a database, and fetch the newest data from network.

The easiest way to do it is producing and consuming the tasks in a sequence, for example:

```Scala
val feeds = getPendingFeeds() // produce the tasks
feeds.foreach(fetchFromNetwork) // consume the dtasks
```

However, it is unnecessarily slow. Network request doesn't take lots of CPU and we can send multiple requests at the same time. Even if `fetchFromNetwork` is a CPU bound task, it can be parallelized if there are multiple CPU cores on a machine.

In this article, we will explore ways to do it more efficiently with [Cats Effect](https://typelevel.org/cats-effect/) and [FS2](https://fs2.io) in a functional programming fashion.

*You may wonder why not using AKKA stream? Other than it's using a different programming paradigm (not functional programming), it's also because [AKKA has changed its license](https://www.lightbend.com/blog/why-we-are-changing-the-license-for-akka) with a ridiculous price.*


## Introducing Cats Effect and FS2

To make `processTask` async, there is `Future` in Scala's standard library. However, the side effect will happen when you create a `Future` instance. For example:

```
def processTask(task: Task): Future[Unit] = Future(println(task))

val runTask1 = processTask(task1) // this will start the async task
```

I assume the readers have a basic understanding of functional programming, so I'll not explain why we want to avoid side effects. While Scala is not a pure functional language, a popular Scala library [Cats Effect](https://typelevel.org/cats-effect/) provides convenient ways to wrap side effects. With the help of its `IO` type, we can define an async task like this:

```
def processTask(task: Task): IO[Unit] = IO(println(task))

// this will not start the task, so no side effect
val runTask1 = processTask(task1)

// out of pure functional world and starts the side effect
runTask1.unsafeRunSync()
```

Then there is [fs2](https://fs2.io) that is a stream library that can be used with cats effect. It will be very handy when resolving our problem as we can see later.

*Cat Effect has some big changes in version 3.x. In this article, we are using version 2.x. But I may upgrade the version in the future.*

## Testing Setup

In order to test which approach is the best under different scenarios, we need some basic setup. In [TestRunner.scala](https://github.com/wb14123/scala-stream-demo/blob/master/src/main/scala/TestRunner.scala), I defined some functions to generate tasks. Here are their signatures:

```
// Produce a sequence of tasks represented by `Int`
def produce(start: Int, end: Int): IO[Seq[Int]]

// Process a task
def consume(x: Int): IO[Unit]

// Produce tasks as a stream
def def produceStream(start: Int, end: Double): fs2.Stream[IO, Int]
```

`produce` simply produces tasks as `int`, and `consume` just print characters. In each of the functions, I use `IO.sleep` to create some delay to simulate the real world non-blocking IO. They also print characters `P` (produce) or `C` (consume) (based on the width of terminal, some of the `C` outputs may be skipped to fit the width) when being invoked, so that we can have an intuitive view of how quick tasks are produced and consumed.

Then there is [TestConfig.scala](https://github.com/wb14123/scala-stream-demo/blob/master/src/main/scala/TestConfig.scala) for configuring the test:

```
trait TestConfig {
  val testName: String
  val produceDelay: FiniteDuration
  val minConsumeDelayMillis: Long
  val maxConsumeDelayMillis: Long
  val batchSize = 100  // consume batch size
  val totalSize = 1000 // how many tasks to generate
}
```

By setting up produce and consume delays, we can test scenarios when producer is slower, consumer is slower, or producer and consumer speed is almost the same. Here are the configurations we are going to use in [Main.scala](https://github.com/wb14123/scala-stream-demo/blob/master/src/main/scala/Main.scala)

```Scala
val configs = Seq(
  new TestConfig {
    override val testName: String = "slow-producer"
    override val produceDelay: FiniteDuration = 1000.millis
    override val minConsumeDelayMillis: Long = 10
    override val maxConsumeDelayMillis: Long = 100
  },
  new TestConfig {
    override val testName: String = "balanced"
    override val produceDelay: FiniteDuration = 1005.millis
    override val minConsumeDelayMillis: Long = 10
    override val maxConsumeDelayMillis: Long = 2000
  },
  new TestConfig {
    override val testName: String = "slow-consumer"
    override val produceDelay: FiniteDuration = 10.millis
    override val minConsumeDelayMillis: Long = 10
    override val maxConsumeDelayMillis: Long = 1000
  }
)
```

## Approach 1: Batch Consuming


The first approach is to make the consuming side parallel. We can consume a batch of tasks concurrently, like in [BatchIOApp.scala](https://github.com/wb14123/scala-stream-demo/blob/master/src/main/scala/BatchIOApp.scala).

```
def loop(start: Int): IO[Unit] = {
  if (start >= config.totalSize) {
    IO.unit
  } else {
    produce(start, start + config.batchSize)
      .flatMap{_.map(consume).parSequence}
      .flatMap(_ => loop(start + config.batchSize))
  }
}
```

However, this only makes a batch of tasks run in parallel. It needs to wait the whole batch to be finished in order to start next batch. This is very obvious when we run this approach and see the output of characters (download [Github repo](https://github.com/wb14123/scala-stream-demo) and run `sbt run "-n BatchIOApp"`). See how it paused after each batch even when consumer is slower than producer:

<script async id="asciicast-P2ZX0r2VYaMXCVjrJCoJ0Y3DS" src="https://asciinema.org/a/P2ZX0r2VYaMXCVjrJCoJ0Y3DS.js" data-rows="10"></script>

## Approach 2: Use Blocking Queue to Buffer Tasks

We need a way to let producers not waiting for consumers, and also let consumers not wait for a batch to finish in order to start next batch. A very common solution is to use a queue between producers and consumers. Producers put tasks into the queue, and consumers get tasks for the queue. If the queue is thread safe, then both producers and consumers can work on their own without care about each other. In order to not let producer put unlimited tasks into the queue to blowup the memory, we need the queue to have a capacity. When the queue is full, the producer should be blocked. And when the queue is empty, the consumers should be blocked as well.

In Java, `BlockingQueue` meets our requirements. We can use an implementation `LinkedBlockingQueue`. However, `BlockingQueue` will block the whole thread instead of a single `IO`. Let's not worry about it for now and see how to use a queue to implement producing and consuming in parallel. The implementation is in [BlockingQueueApp.scala](https://github.com/wb14123/scala-stream-demo/blob/master/src/main/scala/BlockingQueueApp.scala):

```scala

val queue = new LinkedBlockingQueue[Option[Int]](config.batchSize * 2)

override def work(): IO[Unit] = {
  Seq(
    (produceStream(0).map(Some(_)) ++ fs2.Stream.emit(None))
			.evalMap(x => IO(queue.put(x))).compile.drain,
    dequeueStream().unNoneTerminate.parEvalMap(config.batchSize)(consume).compile.drain,
  ).parSequence.map(_ => ())
}

private def dequeueStream(): fs2.Stream[IO, Option[Int]] = {
  fs2.Stream.eval(IO(queue.take())) ++ dequeueStream()
}
```

Here we have two IOs run in parallel with `parSequence`: the first one creates a task stream by `produceStream`, and append `None` at the end so that the consumer knows it should end processing. Another stream `dequeueStream` gets the tasks from the queue then consumes it in parallel with `parEvalmap(config.batchSize)(consume)`.

When run it with `sbt "run -n BlockingQueueApp"`, we can see it's much faster when the consumer is faster or has the same speed as the producer. Especially when the consumer is slow, it prints multiple `P` at first, which means the producers doesn't wait all the consumers to finish in order to produce tasks.

<script async id="asciicast-gWC18DjuVT1v6sDaf2HJJerq6" src="https://asciinema.org/a/gWC18DjuVT1v6sDaf2HJJerq6.js" data-rows="10"></script>

Back to the blocking the whole thread problem: it doesn't seem to be a problem in this case, right? It's only because we are lucky! In this setup, we are using two fixed threads as the thread pool of running IO in `Main.scala`:

```
private val executor = Executors.newFixedThreadPool(2, (r: Runnable) => {
  val back = new Thread(r)
  back.setDaemon(true)
  back
})

implicit override def executionContext: ExecutionContext = ExecutionContext.fromExecutor(executor)

implicit override def timer: Timer[IO] = IO.timer(executionContext)

implicit override def contextShift: ContextShift[IO] = IO.contextShift(executionContext)
```

If 2 consumers with empty queue happens to be scheduled on these 2 threads separately, it will block. If we change our `BlockingQueueApp` to the code in [RealBlockingQueueApp](https://github.com/wb14123/scala-stream-demo/blob/master/src/main/scala/RealBlockingQueueApp.scala):

```
override def work(): IO[Unit] = {
  Seq(
    dequeueStream().unNoneTerminate.parEvalMap(config.batchSize)(consume).compile.drain,
    dequeueStream().unNoneTerminate.parEvalMap(config.batchSize)(consume).compile.drain,
    (produceStream(0).map(Some(_)) ++ fs2.Stream.emit(None)).evalMap(x => IO(queue.put(x))).compile.drain,
  ).parSequence.map(_ => ())
}
```

Here we started two dequeue stream at first. Now the whole program will block when run it with `sbt "run -b"` .

The lesson learned here is that there is a big risk if any operation blocks the whole thread in cats effect. Even it doesn't block the whole program, it may make a whole thread unavailable.


Actually in [Cats Effect's thread model](https://typelevel.org/cats-effect/docs/thread-model), there is another thread pool for blocking tasks if we mark it explicitly. In [AsyncConsole.scala](https://github.com/wb14123/scala-stream-demo/blob/master/src/main/scala/AsyncConsole.scala), I use this exact block mode to run console output so that it won't effect other non blocking IO operations:

```
def asyncPrintln(s: String)(
    implicit cs: ContextShift[IO], blocker: Blocker): IO[Unit] = blocker.blockOn(IO(println(s)))
```

However, if a thread is blocked in this pool, it will start another thread for the next operation. Based on the document, there is no limit on how many threads will be created. So if the producer is much slower than consumer, there will be more and more consume operations blocked on dequeue, so it will generate a large amount of threads, which is not ideal and eventually even will blow up the memory.

## Approach 3: Use Cats Effect Friendly Queue

What if we have a queue that only block the dequeue `IO` when empty instead of blocking the whole thread? Luckily, FS2 provides such a queue. (Cats Effect 3.x also provides such a queue). The implementation is basically the same as above (code in [StreamQueueApp.scala](https://github.com/wb14123/scala-stream-demo/blob/master/src/main/scala/StreamQueueApp.scala)):

```Scala
import fs2.concurrent.Queue

def work(): IO[Unit] = {
  for {
    queue <- Queue.bounded[IO, Option[Int]](config.batchSize * 2)
    _ <- Seq(
      (produceStream(0).map(Some(_)) ++ fs2.Stream.emit(None)).through(queue.enqueue).compile.drain,
      queue.dequeue.unNoneTerminate.parEvalMap(config.batchSize)(consume).compile.drain,
    ).parSequence
  } yield ()
}
```

Run `sbt "run -n StreamAppQueue"` to see how it performs.

## Approach 4: Use FS2 Stream Directly

FS2 actually provides some advanced stream operations that makes it possible to combine the producing stream and consume stream, like the code in [StreamApp.scala](https://github.com/wb14123/scala-stream-demo/blob/master/src/main/scala/StreamApp.scala):

```
produceStream(0).parEvalMap(config.batchSize)(consume).compile.drain
```

Here we map `consume` in parallel on `produce` stream. However, if you try to run `sbt "run -n StreamApp"` vs `sbt "run -n StreamQueueApp"`, you will find this is slower than before. This is because `produceStream` will give the next batch when the downstream asks. If we can prepare at least one batch before the downstream is free, we can save more time. Luckily, it's very easy to do in fs2. As we can see in [PrefetchStreamApp.scala](https://github.com/wb14123/scala-stream-demo/blob/master/src/main/scala/PrefetchStreamApp.scala), we can add `prefetch` after the `produceStream`:

```
produceStream(0).prefetch.parEvalMap(config.batchSize)(consume).compile.drain
```

It will prefetch a [chunk](https://fs2.io/#/guide?id=chunks) of elements. Use `prefetchN` if you want to prefetch N chunks.

Then run this with `sbt "run -n PrefetchStreamApp"`, you will find the performance is similar as the queued approach.

Actually if you check the source code of `prefetch`, you will find the implementation is almost the same as ours:

```
def prefetch[F2[x] >: F[x]: Concurrent]: Stream[F2, O] = prefetchN[F2](1)

def prefetchN[F2[x] >: F[x]: Concurrent](n: Int): Stream[F2, O] =
  Stream.eval(Queue.bounded[F2, Option[Chunk[O]]](n)).flatMap { queue =>
    queue.dequeue.unNoneTerminate
      .flatMap(Stream.chunk(_))
      .concurrently(chunks.noneTerminate.covary[F2].through(queue.enqueue))
  }
```

## Approach 5: Make Producers Run in Parallel

We've made it runs in parallel between consumers, also between consumers and producers. But we haven't made producers run in parallel yet. With the queue, its very easy to do, just start multiple `IO`s for `produceStream.through(queue.enqueue)`. [ConcurrentProduceQueueApp.scala](https://github.com/wb14123/scala-stream-demo/blob/master/src/main/scala/ConcurrentProducerQueueApp.scala) is an example:

```
private val counter = new AtomicInteger(0)

override def work(): IO[Unit] = {
  for {
    queue <- Queue.bounded[IO, Int](config.batchSize * 2)
    _ <- Seq(
      produceStream(0, config.totalSize / 2).through(queue.enqueue).compile.drain,
      produceStream(config.totalSize / 2, config.totalSize).through(queue.enqueue).compile.drain,
      queue.dequeue.parEvalMap(config.batchSize) { x =>
        consume(x).map { _ =>
          if (counter.incrementAndGet() >= config.totalSize) {
            None
          } else {
            Some()
          }
        }
      }.unNoneTerminate.compile.drain,
    ).parSequence
  } yield ()
}
```

It has 2 concurrent producers but in theory you can create as many as you want, just be careful with the parameters of `produceStream`.

If you run this with `sbt "run -n ConcurrentProduceQueueApp"`, you can find the performance is much better with slower producer. However, with the help of fs2 library, we can make the code cleaner without depends on any queue explicitly. Here is what I did in [ConcurrentProducerApp.scala](https://github.com/wb14123/scala-stream-demo/blob/master/src/main/scala/ConcurrentProducerApp.scala):

```
def work(): IO[Unit] = {
  fs2.Stream.emits(Range(0, produceParallelism))
    .map(batch => produceStream(
      batch * config.totalSize / produceParallelism,
      (batch + 1) * config.totalSize / produceParallelism.toDouble))
    .parJoin(produceParallelism)
    .prefetch
    .parEvalMap(config.batchSize)(consume).compile.drain
}
```

Here we use `parJoin` to join multiple producer stream at the same time.

## More

All the approaches above other than the first one uses a queue either implicitly or explicitly. However, under high parallelism and load, every job operating on a single queue may makes this queue a bottleneck. In this case, there is a [work stealing](https://en.wikipedia.org/wiki/Work_stealing) algorithm that each consumers can has its own queue, and whenever a consumer's queue is empty, it steal some tasks from another one. But it's a little bit complex and unnecessary if the load is not so high, so I will not cover it in this article.


## Test Results

Now let's run all the approaches and compare the performance with `sbt "run -n"`. Here are the results:

|                           | slow producer    | balanced       | slow consumer     |
|---------------------------|------------------|----------------|-------------------|
| BatchIO                   | 11086.078637 ms  |29912.377578 ms |10015.51878 ms     |
| BlockingQueue             | 10190.038753 ms  |14195.228189 ms |6495.333179 ms     |
| StreamQueue               | 10138.016643 ms  |14458.443122 ms |6418.078377 ms     |
| Stream                    | 10356.178562 ms  |15655.697826 ms |6560.111697 ms     |
| PrefetchStream            | 10141.110634 ms  |14578.362136 ms |6376.628036 ms     |
| ConcurrentProduceresQueue | 5187.442452 ms   |14395.321922 ms |6576.538821 ms     |
| ConcurrentProducer        | 5198.723825 ms   |14544.247312 ms |6418.078377 ms     |

We can see approaches that parallelize all the parts win the performance game.
