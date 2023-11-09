---
layout: post
title: The Things You Need to Know When Using Apache Sentry
tags: [Sentry, Hadoop, Cloudera, security, Splice Machine]
index: ['/Computer Science/Software Engineering']
---

For the last few days, I was working on integrating [Apache Sentry](https://sentry.apache.org) into our database product Splice Machine. And when doing that, I found the document of Sentry is awful. It takes me much time to figure out what's the role of Sentry and how to configure it. So in this article, I'd like to write some really important things to help understanding Sentry.


## The Basic

From the [official website](https://sentry.apache.org/), Apache Sentry is a system for enforcing fine grained role based authorization to data and metadata stored on a Hadoop cluster. The services in Hadoop can integrate Sentry so that they can use Sentry to manage authorization. You may think this implies that Sentry is a centralized authorization system and you can configure the authorization polices through Sentry, then all the services can use these polices. This is only half true: Sentry is a centralized authorization service but you may not have a centralized place to configure polices or use that policy for all Hadoop services. Let's see the details.


## How to Configure Sentry Policy

When I start learning to use Sentry, the configuration of authorization policy confused me a log. I thought it would be like [Apache Ranger](https://ranger.apache.org/) to have a centralized web UI or at least provide some CLI tool. But it turns out that's not the case. And more confusing, there are two ways to configure Sentry polices: one is file based, which is like a centralized way to configure Sentry policy but is deprecated. Another way is to configure polices from other service, which you will not even notice the exist of Sentry.


### Use Policy File

Let's start with the old and deprecated way: using the policy files. The policy file approach is more straightforward: you write a file to define the authorization policies. The file is usually on HDFS so that every node can access it. You tell the service where to find the file. Then the service will use Sentry library to parse the policy file and get the permissions for every user. So in this way, there is no Sentry service, just policy files and Sentry library to parse the files.

```
          --------
          | User |
          --------
             |
             | <-- Write Policy Files
             V
    ------------------------
    | Sentry Files on HDFS |
    ------------------------
             ^
             | <-- Read and Parse with Sentry Library
             |
    ------------------------
    | Hive/Impala/Solr ... |
    ------------------------

```

For more details like how to configure the services to use Sentry policy file and how to write policy files, you can refer the [Cloudera document](https://www.cloudera.com/documentation/enterprise/5-10-x/topics/cdh_sg_sentry.html).

### Use Sentry Service

Sentry service is a centralized service that other systems can use RPC to request the permissions of a user. If a system is integrated with Sentry service, there is no need to write policy files. However, the system needs to provide methods to configure the policies and save them to Sentry.

For example, Hive and Impala allow user to use `GRANT` and `REVOKE` to configure permissions and it will save the permissions into Sentry service. Then when you query from Hive and Impala, it will ask Sentry service to see if you have the permission. Solr provides a tool to let you define the policies and save to Sentry service, too.


```
               --------
               | User |
               --------
                 |  |
       Query --> |  | <-- Grant Permission
                 V  V
         ------------------------
         | Hive/Impala/Solr ... |
         ------------------------
                 ^  |
  Request    --> |  | <-- Save Permissions
  Permission     |  |
                 |  V
          -------------------
          | Sentry Service  |
          -------------------
```


## Conclusion

So there are two things that confused me a lot and I'd like to highlight here:

1. There are two ways to configure Sentry policies. The are not related and a little like different systems.
2. For the Sentry service, there is no way to configure policies through Sentry service directly.

I think these are both design failures of Sentry. For the first one, if a system changed so much, at least you need to change a big version and highlight it in document. Like Python 2 and Python 3.

For the second one, it is really strange to doesn't have a way to configure and view the policies through Sentry service. If user still need to configure the permissions separately for each system, it is a little point-less to use a centralized authorization service. Unless the permission model is the same or similar, like Hive and Impala basicly use the same permission model, so if you configure the permissions in Hive, Impala can use it. But the situation like that is rare. And I don't think it would be difficult to provide a CLI tool to configure and view Sentry policy.
