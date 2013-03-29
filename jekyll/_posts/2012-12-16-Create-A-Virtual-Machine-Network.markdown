---
layout: post
title: Create A Virtual Machine Network
tags: [qemu, virt-manager, virtualization]
---

Because of some problems I'm working on, I need some test machines with Windows and some Linux distribution installed. It is impossible to have so many machines or install so many systems on my laptop. So I use [qemu](http://www.qemu.org) to create some virtual machines and use [virt-manager](http://virt-manager.org/) to manage them. Here is what I have now:

+ My host: Archlinux, 64 bits, Core i3, 2G RAM, 2G swap.
+ Guest 1: Ubuntu 8.10 desktop, 64 bits, 1 core, 512M RAM.
+ Guest 2: Ubuntu 12.10 server, 64 bits, 1 core, 512M RAM.
+ Guest 3: Windows XP, x86, 1 core, 512M RAM.

There are two points I'd like to say:

+ Qemu with [kvm](http://www.linux-kvm.org/) is quite effective. I could start three guests (or more) at the same time and it works very well.
+ Virt-manager is very easy to use. Before it , I waste almost a whole day to use qemu's command line to put the three guests into a same VLAN.

I'm quite happy with these now. I could

+ Try whatever Linux distribution I'd like.
+ Do experiments in multi-host environment.
+ Test whatever on any OS and need nothing to worry about.

After all, I want to say something about virtaulization. I think the only useful situation of virtaulization is testing, learning or trying to do something for fun. I'm not saying it is not important. When a technology could let us do some funny things, it will be really awesome and important.
