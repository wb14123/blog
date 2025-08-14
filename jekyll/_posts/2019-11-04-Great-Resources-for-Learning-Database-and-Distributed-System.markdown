---
layout: post
title: Great Resources for Learning Database and Distributed System
tags: [database, distributed system]
index: ['/Computer Science/Distributed System']
---

It has been a long time since the last update of my blog. This has been a crazy year. I've prepared for a big change in my life for most of the year. I may write a blog about that in a few days. In this article, I'd like to write something about my work these years: database, big data and distributed systems.

I've been working on data analysis for about 5 years. At my last company, we processed many big companies' data. At my current company, we are building a distributed database that aims to be good at both OLTP and OLAP jobs, with SQL and transaction support. So I learned a lot these years and it is extremely interesting. I have thought about sharing them a lot of times before. But a principle of my blog is sharing new things that can inspire people, even if it is small, instead of repeating old things. Unfortunately, I haven't had many original ideas or works to share. However, when I look back, I find that when I was learning and working, resources were not very easy to find. So in this blog, I'll list some resources that helped me a lot, and I think will help everyone who wants to get familiar with database and distributed systems.

## The Book: [Designing Data-Intensive Applications](https://www.amazon.com/Designing-Data-Intensive-Applications-Reliable-Maintainable-ebook/dp/B06XPJML5D)

This is a book that explains many daily used ideas and practices about database and distributed systems. It clarifies some confusing terms and makes some complex algorithms quite understandable. This book was published in 2017, so it is pretty up to date. Maybe the author doesn't give a deep exploration of every topic, but it is enough to build a solid foundation for the reader to do future study and research. Other than only introducing the things that already exist, the author also gives some new ideas about how data can be stored and processed. Though I don't totally agree with his ideas, they are still very insightful. And in the last chapter, the author talks about data privacy and how we can improve it as engineers. I couldn't agree more. I'm very respectful of speaking it out loud in an engineering book.


## The Course: CMU Database Courses

There are two courses: the [basic one](https://15445.courses.cs.cmu.edu/fall2019/) and the [advanced one](https://15721.courses.cs.cmu.edu/spring2019/). The courses provide a very solid introduction to the important ideas and theories of databases. For example, the implementation of transactions and optimizers, which are not covered in the previous book. They have reading materials and videos, all available online. You can pick the topics you are interested in, so it will not take much time. And they are updated every year so some new research and information are included.

## The Tool: [Jepsen](https://jepsen.io/)

After reading the book and watching the courses, you can at least know that some claims from database companies are just fancy words. For example, some databases claim to support transactions, but at what isolation level? Some databases claim to support multiple nodes, but what's the consistency level? What about availability? Even though they describe them in detail, how do you know the product is the same as the documentation claims? And if you are implementing a database, which is very hard, how can you be confident that you are doing it right? Here comes the awesome tool: Jepsen. It is a tool that can simulate concurrent queries to databases, and introduce errors like network partitions, clock drift and node failure. Then it compares the results to see if they are the same as expected. Actually I've used this tool to find some transaction bugs in our database.

The author of Jepsen has analyzed many databases and written [reports](https://jepsen.io/analyses) about them. I highly recommend reading these reports. You can see what can go wrong in the real world and how to find them. And you can see what database companies claim versus what they really are.

There is also an [outline for the distributed system training](https://github.com/aphyr/distsys-class), which I think is great to see if there is any knowledge you are still not familiar with and then study by yourself.

## The Blog: [DBMS Musings](https://dbmsmusings.blogspot.com/)

This is a blog by Daniel Abadi, a database researcher at the University of Maryland, College Park. He explains some easily confused terms and some trending topics about databases. The only drawback of the blog may be the color of the webpage. The red background is very unfriendly to the eyes. You may want to read the blog in an RSS reader.
