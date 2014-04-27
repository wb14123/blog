---
layout: post
title: Why I Come Back to Erlang
tags: [erlang, node.js]
---

Some months ago, I learned Erlang and wrote a poker game with it. While I'm using it, its strange syntax drives me to crazy. The most sweet syntax I've ever seen is in coffee script. So in the next game I wrote (also a poker game), I tried to use Node.js with coffee script. But after the basic features are implemented, I came back to Erlang. Here is why.

Coffee-script Is Not Easy to Maintain
------------------------

While the coffee project comes big, it is very difficult to maintain. You don't know what's in an object because the members in it could be changed in any time. There is no declarations in the code, so you must write many comments if you want things to be clear.

Coffee-script has no type. I thought the language without type is easy to use at some months ago. But after that, I think language with type is easy to maintain. And it is more friendly to IDEs and editors so that you can write code faster and find out your errors earlier.

Erlang is a dynamic type language, too. But it comes with a very flexible type system and the code could be checked with dialyzer. (Though I prefer to write type informations in the code instead of in the optional `-spec`.)

Node.js Runs the Computation In A Single Thread
-------------------------

Yes, Node.js will fork some threads to do IO. But while you are computing things with CPU, it only do things in one single thread. It is not a big deal if you are writing a web server since it doesn't use CPU often. But it is not true in a game server. A game server has lots things to compute so it will be very helpful if the computation could be run on multi-core. Erlang is known as scalable. It just do these things automatically.

If There Are Some Errors, Everything Fails In Node.js
--------------------------

If something is error in the Node.js application, the whole application will fail. If you are using pm2 or something like that, it will restart the application for you if fails. Again, it is fine in a web application since it is stateless. But the game has a state and many things will be lost after restart.

If an Erlang process fails, it won't affect other processes. It will lost something, too. But it only affect few players if the bug is not easy to trigger(If it is easy to trigger then we've fixed it in the development).


It Is Not Easy To Profiling And Monitoring Node.js Applications
--------------------------

I've reviewed many Node.js monitor tools and found none of them is easy to use. Almost all of them use a third-part website to monitor or profile your application. I'm very confused with it. These things really sucks.

In the other side, Erlang have built in profiling and monitor tools. You can monitor each node, each process and so on.

The Killer Feature Of Erlang: Hot Code Deployment
--------------------------

If all of my reasons are listed until now, then there are many other choices such as Scala, Go and so on. But they are leak of the killer feature of Erlang: the ability of hot code deployment. We don't want to stop the game server at any time. It will affect a lot.

Conclusion
--------------------------

After all, I'm not saying Node.js or Coffee-script is such a bad platform or language that you should never use. But in this case, Erlang is more suitable. Erlang is a platform that provides many tools. I can be fine with its syntax with such a benefit.
