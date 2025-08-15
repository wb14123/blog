---
layout: post
title: Languages Should Have Database Built In
tags: [programming, language, database]
index: ['/Computer Science/Software Engineering']
---

When we are programming, we are programming with data. When the data is too big, we could not put it all in the RAM. So we need some place to store this data, and then read it when we need it. The place we store our data is called a "database".

The steps to use a database are very annoying. We need to write much code to do operations on it. We need to consider many details so that we could read and write more quickly with fewer problems. But why should we not care about it? The only thing we care about is that we could be able to get, store and compute the data. We do not care about how the data is stored.

Doing operations on the database is an art to manage the storage resources (including RAM and hard disk) of computers. Think about the early days. When we use C, we must manage the pointers and RAM by ourselves. But most modern languages have GC built in so that we need not manage RAM manually. So why do we need to manage other storage resources?

Let us have a look at the popular languages and frameworks. PHP is very popular for writing websites. It is very easy to write SQL with it. Ruby on Rails also has an easy way to use database frameworks. And so on.

But I do not think it is enough. I think the ideal situation is to have a database built in it.

There are two ways:

## Database as a Module of the Language

This way is implemented as the standalone databases nowadays. Though we have a standalone database that many different languages could be able to use, none of the languages could be able to use it easily. That is the biggest problem.

There is no need to build the database in the core language in this way. But the database library should be a standard one and the API should be easy to use.

The good part of this way is the structure of different languages' database could be the same, or could be able to transform to each other. So that many languages could be collaborated together.

The bad part is we still realize the existence of the database.

## Database Totally Built In the Language

The other way is to totally build the database into the language. In this way, the data uses the RAM first, just as normal languages do. When the RAM is not enough, it moves the cold data to the hard disk. So that we could just write code normally, avoiding the annoying part of doing operations on the database.

The operations on the databases could be difficult. If the language doesn't resolve it, it just leaves these difficult things to the programmers.

MongoDB is a good starting point. It uses as much RAM as it can. And when the RAM is not enough, it will put the data on the hard disk. It also uses JavaScript as its query language.
