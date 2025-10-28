---
layout: post
title: My First Rust Project
tags: [Rust, LLM, async]
index: ['/Computer Science/Programming Language/Rust']
---

## Less Active Scala Community

Rust is getting more and more popular. It targets low level high performance programming because of zero cost abstraction. For example, it doesn't have any runtime, thus no GC. I have learned C/C++ and used C for programming contests back in university, but in most of my professional career, I use higher level languages like Java, Scala, Python, Go, and so on. All of them have GC built in, so I rarely need to think about memory allocation. For side projects, I have a few criteria to consider when selecting a language:

* The language itself: powerful and flexible syntax. Better to have a type system so more errors can be caught at compile time.
* Good tools to debug and profile the program.
* Mature libraries.
* Active communities.

JVM is a good platform that has wonderful tools and lots of libraries thanks to the popularity of Java. But I really don't like Java the language itself. Thankfully, there are lots of other languages available on JVM, including the one I like most: Scala. Scala was popular when Spark was popular. However, it has been trending down for many years.

With fewer and fewer users, the community is less and less active. It's a negative feedback loop. Many widely used libraries are only maintained by a few people and are not very active anymore. The popular paradigm changed from actor mode with libraries like Akka to functional programming frameworks like Cats and ZIO. I like Cats a lot, but I must admit not everyone can catch the paradigm change. Even for Cats Effect itself, the migration from 2.x to 3.x changed lots of things, and even the most basic IO type doesn't have documentation yet. Projects written in Scala can feel like completely different languages based on the paradigm and libraries they use. Divisions like this make the already small community even smaller, not to mention some drama between Cats and ZIO.

So even though I still like the language, and I've written lots of my personal projects with it, I started to think twice when starting new projects. Sometimes I find the smaller community starts to hurt productivity when I run into something strange, and the complexity of the language makes it hard to read the library code in order to understand what's going on internally. With the [Loom project](https://wiki.openjdk.org/display/loom/Main), I feel like I may write my own light wrappers around virtual threads with functional programming instead of using Cats Effect. But that's another topic.

The development of LLM makes the problem worse. With a less active community and fewer documents, LLM has less data to train on the language, which makes it less good at writing code with Scala compared to other languages. Even though I don't think LLM can write code all by itself yet, it's still a good tool, and it's a disadvantage if it doesn't work well.

Nevertheless, I'll still use Scala in my projects for the foreseeable future. But I want to learn and see something new.

## The Failed Attempt at Learning Rust

I was interested in Rust in its early days. I started to read the Rust book around 2016 and tried to write some Rust code in the following 2 years. Especially after I joined a database company. I wanted to write some database related things by myself. I want the performance to be as good as possible, so I want a language without GC. Rust felt like a natural choice at the time. So I attempted to implement an [LSM tree](https://en.wikipedia.org/wiki/Log-structured_merge-tree) and started with a [skip list](https://en.wikipedia.org/wiki/Skip_list), which is a popular data structure to implement the memtable part of an LSM tree. It's like a linked list but more complex. Oh boy, was I wrong to try that for my first Rust project. Rust's pointers are really bad at self referencial structures if you don't want to use `unsafe`. Writing a linked list with Rust remains to be a hard problem nowadays. It almost becomes a meme for Rust new learners, very much like the joke about how to exit Vim. So it's not a surprise that I failed miserably and gave up at last. My interest in databases also transferred from high performance data structures to the correctness of distributed systems, so I didn't look back at Rust for a long time. Until recently, I wanted to write some database related things again. With more concerns with Scala and with Rust being more and more popular, I decided I must learn it.

## The First Project

Learning from the first failed try, my goal this time is to implement something easy enough that doesn't need to handle low level memory structures. I just want to get a sense of how Rust feels when just writing some "regular" programs. In order to keep my interest, the project needs to be fun. After thinking through a few candidates, I ended up writing a program to talk with multiple customizable LLM bots at the same time: you can create the bot profile and add them to a chat room. When chatting, the LLM decides which bot replies next and then uses that bot's profile to reply. The code is in my GitHub repo [v-world-cli](https://github.com/wb14123/v-world-cli).

This projects meets all the requirements above. While being lots of fun, I can explore things like reading files, network requests, async programming, streaming and so on. If I want, I can continue to explore things like database integration and web server. I just implemented the minimal feature set. Lots of the features like saving the conversation history, room profile, agent memory and so on are not implemented, and probably never will be.

## Learn with the Help of LLM

LLM coding agents make the learning process much faster, in mainly two ways. First, you can implement some non-important components of the projects with LLM to see the results faster and refine them later manually. This makes the feedback loop much shorter. It's great to see some results when implementing something instead of waiting until every component fits together. For example, in my case, I don't really care about the UI part, so I just let the LLM create the module based on the interface I've already defined. Similarly, after I defined the interface, I also let it write the code for integrating with OpenAI compatible APIs so that I don't need to create all the API structures and look for the documentation. This way I can chat with the bots much earlier and be motivated by the result.

The second is the more traditional usage of the LLM coding tool: letting it fix the problems in the code. Rust is notorious for the difficulty of ownership reasoning and many pointer types. With LLM's help, I can come up the fix much faster. It doesn't always come up with the best answer, but with it pointing a direction, I have a starting point to research instead of not knowing where to look. For example, there is a `Pin` type sometimes needed with async functions. Without LLM's help, I think I would spend much longer time realizing I need to use it to fix some compile errors.

## The Beauty and Ugly of Rust

The beauty of Rust is that with the help of its type system, the language can feel like a GC language since you don't need to explicitly free the memory. It binds the lifetime of the variables to a scope and provides different types to manage it when you want it to outlive the closure scope.

The ugly of Rust also usually comes from it. Sometimes you just feel the types are just in your way of implementing something. You need to know too many implementation details in the libraries in order to fight the compile errors. The abstraction of the library is leaking. For example, sometimes you need to use the `Pin` type because of some implementation details of `async`.

Other than the ownership system, some syntax of Rust feels ugly compared to languages like Scala. For example, in Scala, in operations like `map` and `filter`, if you only need the variables once, you can use `_` instead of defining the parameter list in the anonymous function, e.g.,

```scala
arr.map(_ + 1) // add 1 to all the elements
```

With Rust, you need to write the parameters explicitly:

```rust
arr.map(|x| x+1);
```
Or if the type doesn't implement `map` and you need to use `iter`:

```rust
arr.iter().map(|x| x+1).collect();
```

This is more obvious with `match`. In Scala, you don't need to use the `match` keyword all the time:


```scala
results.map {
  case Some(r) => "I have a result"
  case None => "empty result"
}
```

But with Rust:

```rust
results.map(|x| match x {
  Some(r) => "I have a result"
  None => "empty result"
})
```

With that said, the syntax of Rust is already much better than lots of other languages, so it's really not a big deal.

## Different Pointer Types

I'm still learning the best practice of using different pointer types at the right time, but here are the ones I used in my first project:

`&` for passing a reference without changing ownership. Use this when possible since it has the least overhead and is the simplest. `&mut` is its mutable version.

But sometimes the lifetime of the value will live outside of the current scope, especially when using `async` with `spawn`, which doesn't guarantee the new process to be finished before the current scope is ended. In this case, `Arc` is very useful. This type tracks the reference count of the variable and frees it when the reference count is zero, very much like GC for other languages, but the overhead is much lower because it tracks and frees memory at the end of the scope instead of having a separate process to scan all the variables. The references are read only. If you need a writable structure, then use it with `RwLock` like `Arc<RwLock<T>>`.

At last, `Box` for things that don't know the size at compile time. For example, the most common use case is the error part in `Result`: `Result<T, Box<dyn Error>>`.

## What's Next

I want to explore macros in Rust to see if I can implement an easy to use Raft library. Then maybe implement some toy distributed file system based on it.
