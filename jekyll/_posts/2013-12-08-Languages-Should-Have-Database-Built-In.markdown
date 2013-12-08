---
layout: post
title: Languages Should Have Database Built In
tags: [programming, language, database]
---

When we are programming, we are programming with data. When the data is too big, we could not put them all in the Ram. So we need some place to store these data, and when we need to use these data, we must read them. The place we store our data is called "database".

The steps to use a database is very annoying. We need to write much code to do operations on it. We need to consider of many details of the database so that we could read or write more quickly. But we should not care about it. What we care about is that we could be able to get, store and compute the data. We do not care about how the data is stored.

Do operations on the database is an art to manage the storage resources (include Ram and hard disk) of computers. Think about the early days. When we use C, we must manage the pointers and Ram by ourself. But most modern languages have GC built in nowadays so that we need not manage Ram manually. So why we need to manage other storage resources?

Let us have a look at the popular languages and frameworks. PHP is very popular to write websites. It is very easy to write SQL with it. Ruby on Rails also have an easy way to use database framework. And so on.

But I do not think it is enough. I think the ideal situation is:

* The database should built in the language so that there is no gap between logic and data.
* The databases built in different languages should have the same structure, or could be transform between them.

The second one is implemented in nowadays. They are implemented as the standalone databases. But the more important one is the first one. Though we have a standalone database that many different languages could be able to use, but none of them could use the database easily. That is the biggest problem.

There is no need to build the database in the core language. But the database library should be a standard one and the API should be easy to use.

We know there are many difficult fields to design a database. And more difficult to build it into a language. But if the language do nothing about it, the difficult things all leaved to the users.
