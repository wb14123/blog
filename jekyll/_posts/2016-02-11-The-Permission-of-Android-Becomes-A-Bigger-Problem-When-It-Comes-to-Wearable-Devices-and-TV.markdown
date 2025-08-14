---
layout: post
title: The Permission Management of Android Becomes A Bigger Problem When It Comes to Wearable Devices and TV
tags: [Android]
index: ['/Computer Science/Operating System/Android']
---

The permission management of Android has been a problem for a long time. The behavior of an app depends on the morality of its developers. Unfortunately, many developers are big companies whose primary target is making money and morality is little considered.

The problem gets bigger when it comes to devices with less power than smart phones such as TVs, or devices that can get more privacy such as smart watches. The main permission problems of Android are:

## You must give all the permissions to an app if it requests

On Android, you must give all the permissions it requests, or you just cannot install this app. This behavior has been optimized since Android 5.0. But it is far from enough. Though we can also modify the permissions with tools such as Xposed, it is very difficult for normal users and not all devices can use it.

There is also another way to solve this: provide a more trusted app market. Think about Linux: there is barely permission management in most Linux distributions. (User and group management don't count, because a normal user can do many things). But most Linux distributions have a software repository which is maintained by trusted developers, so as long as we install programs from the official repo, we can think we are safe.

This may not be a very big problem for a smart phone because you can install tools to manage it, or you just don't care if your contact information is uploaded to some server. But when it comes to a smart watch, you are wearing it all day alone, it can get much more information than a smart phone. I cannot give that information to a random app.

This is also a big problem for a smart TV. It is very difficult to root it or install custom tools. And a TV is in a family network, which is on the same network as your PCs and laptops. It can also get a lot of private information.


## Many apps always running in the background

Android allows apps to run in the background and awaken each other. It is really a bad design to allow an app to awaken another one in the background. If I'm using an app and it opens another one in front of me, I can see it and know what is happening. That is the only situation I may want such behavior.

Allowing random apps to run in the background hurts performance a lot. You cannot control what the app is doing in the background. Maybe you will not notice this on a smart phone which is more and more powerful nowadays (or you may have noticed that your smart phone is losing power quicker and quicker as you install more and more apps). But the problem appears a lot when the device is not so powerful, for example, a smart watch or a smart TV. This happens in my real life. My family bought a TV some days ago, but I cannot install more than 4 video apps, or the TV will get stuck. It really sucks.
