---
layout: post
title: Experiment On Combining OOP With Erlang's Actor Model
tags: [erlang, elixir]
index: ['/Computer Science/Programming Language/Erlang']
---

Erlang's actor model is good to use, but its syntax is not. Elixir is a very great language, but I don't think it is enough. So I hacked it a little and did some experiments on turning it into an OOP language in some way.

OOP Suits the Actor Model Better
-------------

Erlang is a pure functional language. There are some arguments between OOP and FP. The point of pure functional programming is it avoids side effects: the function always gives the same result while the inputs are the same. In Erlang or Elixir, it is true with the functions. But when there comes an actor (a `gen_server` for example), it is not so true. Let's look at a `gen_server` module for example:

```
-module(example).

init(_Opts) -> {ok, []}.

handle_call(Msg, _from, State) -> {reply, State, [Msg | State]}.
```

It is FP for now. The state of the actor is one of the inputs, so it always gives the same result while the inputs are the same.

But when you call the methods from the outside, this is not so true. For example:

```
{ok, Pid} = gen_server:start_link(example, [], []),

%% A and B will not be the same here
A = gen_server:call(Pid, 1)
B = gen_server:call(Pid, 2)
```

See? In this example, the two calls to `gen_server:call` with the same inputs will not give the same outputs. It is because the actors **do have state**. So I think it is more suitable to think it in an OOP way.

It Is Complex to Define a Good Actor in Erlang
------------------

After I wrote Erlang programs for about half a year, I realized that it puts too much work on the programmer which could be done by the compiler or library. For example, in order to define a module with `gen_server`, we need to do these things:

1. Define a module which behaviour is `gen_server`, and define the callbacks, and an API `start_link` which will be invoked by the supervisor.
2. Define a module which behaviour is `supervisor` to supervise these actors.
3. Define a module with the API, which will invoke `gen_server:call` or `gen_server:cast` to send messages to the `gen_server` that is just defined.

Elixir reduces the complexity of Erlang's syntax a lot. But it doesn't reduce the steps above. I will write a library that does these things for the programmer, in an OOP way.

My Implementation
-----------------------

Things should be done just like this:


```
#this defines a `gen_server`
defmodule Basic do
  use Eroop

  init _(init_count) do
    # "@" means the state in actor
    @counter = init_count
  end

  async add(num) do
    @counter = @counter + num
  end

  sync get do
    @counter
  end
end

# this starts a `gen_server`
c = Basic.new 2
# this will be executed asynchronously
c.add 1
# this will be executed synchronously and get its result
count = c.get

```

I think I don't need to explain much about it if you are familiar with Erlang, actor model or Elixir. Thanks to the powerful Elixir and its macro, I've implemented it in a clean way. (View the source code [on Github](https://github.com/wb14123/eroop)).

The Problems
--------------------

But there are also some problems here. The first one is: I would expect the supervisor to work.

For example, I'd like this code to work:

```
defmodule Crash do
  use Eroop

  init _(init_count), do: @counter = init_count
  async crash, do: 0/0
  sync get, do: @counter
end

Crash.start_sup
c = Crash.new 1
# Though it is crashed, the supervisor should restart it and assign its new pid to `c`
c.crash
count = c.get
```

The fact is, the supervisor is able to restart it, but the variable `c` lost the pid of the newly started server, so `c.get` will not succeed. There are some ways to fix it, doing a little hack and registering `c` as the `gen_server`'s name is one of them.

But the code will be complex, and it pushes me to think:

1. Erlang is not an OOP platform.
2. I still think OOP suits the actor model better than FP.

You must want to remind me about Scala. But its actor syntax is as worse as Erlang's. And it is not as easy to extend its syntax as Elixir.

Next Plan
------------------------

So, what's my choice? My choice is to stop thinking about the actor model for a moment and start to learn some Haskell. I've heard Haskell solves concurrency problems well and is cleaner to build big applications with pure functions. I want to have a look at it. So learning Haskell is my next plan. You can wait to see my posts about it!
