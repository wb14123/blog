---
layout: post
title: Server Logic of Level Based Games
tags: [game, server, unity3d]
---

These days, I'm working on the server side of an ARPG mobile game. The client of this game is written with Unity3d. I've never done such a thing before. After thinking and researching many days, I found out how to implement the server logic in such a "level based game".

What is "Level Based Game"?
------------------------------

Level based game means it is not a typical client-server game. In a level based game, player (or with his friends) could play the game in a level, isolated from other players. So most of mobile games are level based games, such as Angry Birds, Doodle Jump and so on. The more suitable examples are WarCraft3, StarCraft and Dota2. A "level based game" will have all the logic on the client, so that the player could play in a level without the Internet available.

A "level based game" could do these things:

* User could play in a level without any network available.
* User could play with users on the Internet if the Internet is available.
* User will get prizes after a level completed.
* User could not be able to cheat.

What Should A Server Do?
-------------------------------

A game such as Angry Birds or Doodle Jump need no server. But some RPG games such as [Eternity Warriors](https://www.youtube.com/watch?v=QRvD5Om6w3c) need a server to validate if the player was cheating. The validation is not as same as the traditional MMO-RPG games on PC. Client has all the logic of the game in order to play without any network available. So the server don't need to implement the logic things about the game. Things the server need to do are:

1. Validate if a player was cheating.
2. Broadcast messages if a user plays with his friends.

I will not talk about broadcast messages in this article.  People do researches on it since "server" has born. I just want to talk about how to validate if the user was cheating on the logic. "On the logic" means the validate methods in this article will be useless if the player use a robot to play.

The goals of the validation are:

* Check fast.
* Check exactly.
* Easy to implement.

Online Validation
--------------------------------

A user need not to connect to the server when he plays alone. But his data need to be validated. So we store his actions in the level. Pack and send them to the server after the level completed. Server will validate the package, and send a response. If the time to response is reasonable for the user, then it can be thought as an online validation.

If the player's data did not pass the online validation, he can not get the prize of this level.

Online validation just validate parts of the logic. We consider efficiency and simplicity, then decide which parts are going to be validated.

### Server Efficiency

Server will be efficiency if it is event based. But most of ARPG games are in a main loop, it use lots of CPU resources. And almost all of RPG games have collision detection, which will also waste the server resource. So the server will just validate the steps which don't wast resources.

The advantage of the server don't have a main loop is, it could run really fast. It just compute real things, never waste the resources in a loop and wait for something.

### Server Simplicity

Almost all of the developers use a game engine to develop a game. Most of them don't own these game engines, and lots of these engines are closed source. So they cannot know the low-level implementation of the game engine, which are also useful for the server if you want to validate the data more exactly. But as we have seen, the client side already has all of the logic, should we rewrite a game engine that has the same behaviour as the client's? No. That is a lot of work!

So which parts are **not** going to be validated?

* The expensive parts.
* The difficult parts.

So the parts are not going to be validated are collision detection, path finding, AI and so on.

Offline Validation
--------------------------------

Online validation is not enough for the clever cheaters. So we need a more powerful way to validate.

The most powerful way is validate all the logic: replay the player's actions, and see if the result is the same. It will be difficult to implement the same logic as the client's if the client use a game engine. So the idea is, we just use the client application to do it. As far as I know, the Unity3d standalone could be able to run on a Linux server without GPU.

The replay mechanism is very useful in many other ways. It could help debug, review user's data manually and so on.

This method will use lots of server resources. So the validation is offline. Which means the prizes of the level are given. And we only do offline validation when the server has enough resource. And when we found a player is cheating, he will be punished.

Machine Learning Algorithms
----------------------------------

Offline validation will use lots of resources. When there are many players, it may not be able to validate all the packages. So we need to use machine learning algorithms to recognize which packages may unusual, then use offline validation. There are many machine learning algorithms could be used, such as classify algorithms.

When an algorithms have a high accurate rate and it is fast enough, we could use it as a part of online validation.


Multi-Player Implementation
----------------------------------

The implement of multi-player is easy. Since many players are playing, the messages are send to the server as a stream instead of a package. So we just run the online validation at the same time when the users are playing. When we found one of these players is cheating, just break off this level. Other parts will still be the same.

Conclusion
------------------------------------

So the server architectural include:

* A server: receive players' action.
* Online validation workers: fast/simple validation.
* Offline validation workers: slow/exact validation.
* Classify workers.
* Database.

The database will store the packages of players' actions. They can be stored as 6 types:

1. Online validate failed.
2. Online validate success.
3. Unusual.
4. Usual.
5. Offline validate failed.
6. Offline validate success.

Here are how the server works. It will include online part(server, online validation worker, database), offline part(offline worker, database) and classify part(classify worker, database). The input is the packages of the players' actions. And the bold font is the output.

### Online Part

1. Server receive the players' actions data from client.
2. Use an online worker to validate if the user is cheating.
3. **Don't give the prize** if the user is cheating. Label the package as type 1.
4. **Give the prize** otherwise. Label the package as type 2.
5. **Respons the result to client**. Put the package in the database.

### Classify Part

Classify algorithms use data in database (type 1, 5 as cheating package, and type 6 as not cheating package) to train itself.

Let's see how classify workers label the data in database:

1. Get a package labeled as type 2.
2. Use classify algorithms to validate if it is usual. And label it as type 3 or 4.

### Offline Part

1. Get a package labeled as type 3.
2. If no package is labeled as type 3, get a package labeled as type 4.
3. Use a offline worker to validate if the user is cheating.
4. **Punish the user** if the user is cheating. Label the package as type 5.
5. Label the package as type 6 otherwise.


Reference
-----------------------------------

Game servers for Unity3d:

* [Photon](http://cloud.exitgames.com/)
* [SmartFoxServer](http://www.smartfoxserver.com/)
