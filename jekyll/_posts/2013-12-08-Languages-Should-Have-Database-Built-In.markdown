---
layout: post
title: Languages Should Have Database Built In
tags: [programming, language, database]
---

When we are programming, we are programming with data. When the data is too big, we could not put them all in the Ram. So we need some place to store these data, and then read them when we need them. The place we store our data is called "database".

The steps to use a database are very annoying. We need to write much code to do operations on it. We need to consider many details so that we could read and write more quickly with less problems. But why we should not care about it? The only thing we care about is that we could be able to get, store and compute the data. We do not care about how the data is stored.

Do operations on the database is an art to manage the storage resources (include Ram and hard disk) of computers. Think about the early days. When we use C, we must manage the pointers and Ram by ourself. But most modern languages have GC built in so that we need not manage Ram manually. So why we need to manage other storage resources?

Let us have a look at the popular languages and frameworks. PHP is very popular to write websites. It is very easy to write SQL with it. Ruby on Rails also have an easy way to use database framework. And so on.

But I do not think it is enough. I think the ideal situation is to have a database built in it.

There are two ways:

## Database as a Module of the Language

This way is implemented as the standalone databases in nowadays. Though we have a standalone database that many different languages could be able to use, none of the language could be able to use it easily. That is the biggest problem.

There is no need to build the database in the core language in this way. But the database library should be a standard one and the API should be easy to use.

The good part of this way is the structure of different languages' database could be the same, or could be able to transform to each other. So that many languages could be collaborated together.

The bad part is we still realize the exists of the database.

## Database Totally Built In the Language

The other way is to totally build the database in the language. In this way, the data uses the Ram first, just as the normal languages. When the Ram is not enough, it moves the cold data to the hark disk. So that we could just write code normally, avoiding the annoying part to do operations on the database.

The operations on the databases could be difficult. If the language don't resolve it, it just leave these difficult things to the programmers.

Mongodb is a good start point. It use as many Ram as it can. And when the Ram is not enough, it will put the data in the hard disk. It also uses Javascript as its query language.
