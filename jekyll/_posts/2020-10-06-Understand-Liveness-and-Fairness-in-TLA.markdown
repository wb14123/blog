---
layout: post
title: "Understand Liveness and Fairness in TLA+"
tags: [TLA+, Formal Proof, Distributed System, liveness, fairness]
index: ['/Computer Science/Distributed System']
---

Recently I'm learning [TLA+](https://lamport.azurewebsites.net/tla/tla.html): A language that can specify distributed and concurrent systems. Though it's very different from most programming languages, the idea behind it is very simple: basically what it does is specifying a state machine. The [TLA+ tool box](https://lamport.azurewebsites.net/tla/toolbox.html) has a model checker called TLC that can explore all the states of the state machine and check properties of the system. If the state space is too big or infinite, we can define a reasonable subset of it to check. So it will not always guarantee the correctness. However, the tool box also has a more advanced tool called TLA+ Proof System (TLAPS) to write formal proof like Coq. I highly recommend the [video course](http://lamport.azurewebsites.net/video/videos.html) to learn TLA+. It's short and includes TLC. I first started with The TLA+ Book *Specifying Systems* which doesn't include TLC, and I was wondering how specify a system can check properties of it.

Even though many programmers may not be very comfortable with the concept of TLA+ at first, it shouldn't take a lot effort to write a specification. However, I did have some hard time to understand liveness and fairness in the last two videos. The video does a great job to define and explain it. But the example it uses is not very simple which adds a barrier to understand the concepts. In this article, I want to introduce a much simpler example, which makes it much easier to do experiment with it and see if your understanding is right.

This example is a very simple state machine, which state can go from `a` to `b` to `c`:

```
a -> b -> c
```

The TLA+ code to specify it is also very simple:

```
VARIABLE state

Init == state = "a"

AToB == state = "a" /\ state' = "b"
BToC == state = "b" /\ state' = "c"

Next == AToB \/ BToC

Spec == Init /\ [][Next]_state
```

The specification defines what's the possible states and steps of the system. This is **safety property** which defines what a system can do. If we want to use TLC to check anything about this system, we need to select "Temporal formula" under "What is the behavior spec" and put "Spec" in it. For example, if we want to check `state` is always be one of `a`, `b` or `c`. We can check this formula in TLC as an invariance:

```
state \in {"a", "b", "c"}
```

The property we want to check in the system may not always be this simple. For example, we may want to check `state` can be `c` at some point. The property that defines what a system must satisfy is called **liveness**. I found this name confusing at first. I think the reason it's called liveness is because it usually defines what property a system can eventually reach, which means the system is making progress thus "liveness".

So let's define the liveness property that says the system can eventually reach the state `c`. TLA+ uses `<>` to define the meaning of eventually, so the property can be written like this:

```
<>(state = "c")
```

We can add this to TLC model's properties field and run it to check if the system satisfies the property.

So far, if we run this against `Spec`, TLC will report error. What happened? It turns out the specification not only specifies how the state can be changed in next state, but also specifies the state can be unchanged during steps. This makes it possible to interact with other systems. So the states of the system can stuck in one state forever and may never reaches `c`:

```
a -> b -> b -> b -> ....
```

This doesn't seem right. We don't want the system stuck in one state forever. This is where fairness comes in. We can see in the situation above, the state is always in `b`, which `BToC` is enabled and we want it to be executed at some point:

```
a -> b -> b -> b -> ... -> c
```

More specifically, we want the behavior to be executed at some point if it's enabled continuously. This is called **weak fairness**.

So let's add weak fairness to all the next steps of our specification. Then `Spec` changed in to this:

```
Spec == Init /\ [][Next]_state /\ WF_state(Next)
```

If we run TLC again, we can see `<>(state = "c")` will pass the check.

So far so good. But let's make our state machine a little more complex by adding a step from `b` to `a`:

```
a <---> b --> c
```

The corresponding TLA+ specification is like this:

```
VARIABLE state

Init == state = "a"
AToB == state = "a" /\ state' = "b"
BToA == state = "b" /\ state' = "a"
BToC == state = "b" /\ state' = "c"

Next == AToB \/ BToA \/ BToC

Spec == Init /\ [][Next]_state /\ WF_state(Next)
```

Let's check `<>(state = "c")` in TLC again, and we can find it failed. What's happening now? It turns out even though the system will not stuck in one state forever, it can stuck in some of the states, in this case, `a` and `b`:

```
a -> b -> a -> b -> a -> b -> ....
```

Sometimes this is the expected behaviour, but sometimes we want other steps have a chance to happen. In this case, we want to `BToC` have a chance to happen if `state` reached `b` repeatedly:

```
a -> b -> a -> b -> a -> b -> .... -> c
```

We call this kind of property **strong fairness**: if a behavior is enabled repeatedly, it should be executed at some point. So let's add strong fairness to `BToC`.

```
Spec == Init /\ [][Next]_state /\ WF_state(Next) /\ SF_state(BToC)
```

After this, the check of `<>(state = "c")` can pass again.

Let's sum it up. Liveness is a property that the system must satisfies and can be checked with TLC. It usually defines that the system can eventually reach a state. In this case, it's `<>(state = "c")`. Weak fairness is a part of the specification that says a behavior will eventually happen if it's enabled continuously, which means `a -> b -> b -> b -> ... -> c` in this example. Strong fairness is also a part of the specification, which says a behavior will eventually happen if it's enabled repeatedly, which means `a -> b -> a -> b -> ... -> c` in this case.
