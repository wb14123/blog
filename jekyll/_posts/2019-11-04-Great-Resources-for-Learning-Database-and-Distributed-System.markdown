---
layout: post
title: Great Resources for Learning Database and Distributed System
tags: [database, distributed system]
index: ['/Computer Science/Distributed System']
---

There has been a long time since the last update of my blog. This has been a crazy year. I've prepared for a big change in my life in most time of the year. I may write a blog about that in a few days. In this article, I'd like to write something about my work these years: database, big data and distributed system.

I've been working on data analysis for about 5 years. And at my last company, we processed many big companies' data. At the current company, we are building a distributed database aims to be good at both OLTP and OLAP jobs, with SQL and transaction support. So I learned a lot these years and it is extremely interesting. I have thought about sharing them a lot of times before. But a principle of my blog is sharing new things that can inspire people, even it is small, instead of repeating old things. Unfortunately, I haven't have many original ideas or works to share about. However, when I look back, I find when I was learning and working, the resource is not very easy to find. So in this blog, I'll list some resources that helped me a lot, and I think will help everyone that wants to get familiar with database and distributed system.

## The Book: [Designing Data-Intensive Applications](https://www.amazon.com/Designing-Data-Intensive-Applications-Reliable-Maintainable-ebook/dp/B06XPJML5D)

This is a book that explains many daily used ideas and practises about database and distributed system. It clarified some confused terms and makes some complex algorithms quite understandable. This book is published at 2017, so it is pretty up to date. Maybe the author doesn't give a deep exploration for every topic, but it is enough to build a solid foundation for the reader to do future study and research. Other than only introduce the things that already exists, the author also gives some new ideas about how the data can be stored and processed. Though I'm not totally agree with his idea, it is still very insightful. And at the last chapter, the author talks about the data privacy and how can we improve it as an engineer. Which I cannot agree more. I'm very respectful for speaking it out loud in an engineer book.


## The Course: CMU Database Courses

There are two courses: the [basic one](https://15445.courses.cs.cmu.edu/fall2019/) and the [advanced one](https://15721.courses.cs.cmu.edu/spring2019/). The courses provide a very solid introduction to the important ideas and theories of database. For example, the implementation of transaction and optimizer, which is not covered in the previous book. It has reading materials and videos, all available online. You can pick the topics you are interested in, so that it will not take much time. And it is updated every year so some new researches and information are included.

## The Tool: [Jepsen](https://jepsen.io/)

After reading the book and watching the course, you can at least know some claims from the database company are just fancy words. For example, some database claims to support transaction, but at what isolation level? Some database claims to support multiple nodes, but what's the consistency level? What about the availability? Even though they describe them in details, how do you know the product is the same as the document claims. And if you are implementing a database, which is very hard, how can you be confidence that you are doing it right. Here comes the awesome tool: Jepsen. It is a tool can simulate concurrent queries to database, and introduce errors like network partition, clock drift and node failure. Then it compares the results to see if it is the same as expected. Actually I've used this tool to find some transaction bugs in our database.

The author of Jepsen has analysed many databases and write the [reports](https://jepsen.io/analyses) of them. I highly recommend to read these reports. You can see what can go wrong in the real world and how to find them. And what the database company claims and what really it is.

There is also an [outline for the distributed system training](https://github.com/aphyr/distsys-class), which I think is great to see if there is any knowledge you are still not familiar with and then study by yourself.

## The Blog: [DBMS Musings](https://dbmsmusings.blogspot.com/)

This is a blog by Daniel Abadi, a database researcher at University of Maryland, College Park. He explains some easy to confuse terms and some trending topics about database. The only fallback of the blog maybe the color of the webpage. The red background is very unfriendly to the eyes. You may want to read the blog in an RSS reader.
