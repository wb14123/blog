---
layout: post
title: The Proper Way to Use Spark Checkpoint
tags: [spark]
index: ['/Computer Science/Data Processing']
---

These days I'm using Spark streaming to process real time data. I'm using `updateStateByKey`, so I need to add [checkpointing](https://spark.apache.org/docs/latest/streaming-programming-guide.html#checkpointing), which is a fault tolerance mechanism of Spark streaming. The checkpoint will save DAG and RDDs. So when you restart the Spark application from failure, it will continue to compute.

But there is a problem with checkpointing: you cannot load the checkpointed data once you change the class structure of your code, so the state in `updateStateByKey` is lost. This is a pretty big limit. Another solution is to save and load data by ourself, but in this way checkpointing is totally useless and will also break the fault tolerance. What about to use both ways? Then the data may load twice while the application is auto restarted by the Spark cluster, in the case of failure. So I asked this question in the Spark user list and somebody kindly give me [a solution](https://mail-archives.apache.org/mod_mbox/incubator-spark-user/201509.mbox/%3CCAD_32VVBit6eqNhRb5axf4Quxk86v_ZkjFL4ZdziNZrCyT2qEA@mail.gmail.com%3E): use `updateStateByKey` with the parameter `initialRDD`.

The answer is a little simple, so I will explain it here. This way is to use both checkpointing and our own data storage mechanism. But we load our data as the `initalRDD` of `updateStateByKey`. So in both situations, the data will neither lost nor duplicate:

1. When we change the code and redeploy the Spark application, we shutdown the old Spark application gracefully and cleanup the checkpoint data, so the only loaded data is the data we saved.

2. When the Spark application is failure and restart, it will load the data from checkpoint. But the step of DAG is saved so it will not load our own data as `initalRDD` again. So the only loaded data is the checkpointed data.
