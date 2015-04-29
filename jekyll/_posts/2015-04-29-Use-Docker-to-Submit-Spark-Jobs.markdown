---
layout: post
title: Use Docker to Submit Spark Jobs
tags: [Docker, Spark]
---

These days I'm working on analyze data with Spark. Since our Spark cluster is offline in the office for now, so it needs to download data from log server every hour, analyze them with Spark and then upload to the server. The work flow is a little complex so I write some scripts to do it. In addition, I also write a whole automated end to end test for it.

The whole thing is messed up with cron job configurations, shell scripts, MongoDB scripts and Spark jobs. Then I realize, I can pack them into a container, which do all the dirty things while not mess up the outside world. The better thing is, since I am using the CDH cluster, I can download the YARN configuration while build the container.

Here is how the container is built up:

* Compile the spark jobs and run unit tests.
* Install Hadoop client, Spark and Hive client, and download configuration files from CDH cluster.
* Install cron jobs to the system.
* Generate data and test if the whole work flow works.

The container will do these things every hour through cron job:

* Download data from log server.
* Sends the data to the CDH cluster and submit the spark job to it.
* Fetch result data from CDH cluster and upload them to the online server.

You may wonder why not just use some work flow tools in Hadoop ecosystem like Oozie? First of all, Oozie uses XML as its config file which I think is very complex. And it is also less flexible. With Docker container, I can build it and test it without touch the outside world. And it is just a very flex component that can be attached to the Spark cluster easily with a single command. Or think the Spark cluster as a low level service, which just provides computing resource and should not care about other things.
