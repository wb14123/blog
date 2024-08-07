---
layout: post
title: My MacOS Essentials
tags: [MacOS, tools, software, desktop environment]
index: ['/Computer Science/Operating System/MacOS']
---

As a long time Linux and KDE user, I'm pretty uncomfortable with the workflow of MacOS even though I have used MacOS fairly long as well. A lot of companies don't support Linux to be used on the development laptop. Even for some companies that do support Linux, the hardware for Linux is usually far worse than Macbooks. So MacOS is often the best or even the only choice for work. This is still the case for my new job. I think it's a good opportunity to write a blog about my MacOS setup. This can be a note for myself when I need to setup a fresh MacOS again in the future.

## My Complain about MacOS Desktop Environment

Everyone has different taste and needs about desktop environment and I respect that. The following is just based on my own preference. If you happen to have the same pain points, the setup may help you. Otherwise I find it's pretty inspiring to see how other people work as well even though I may never work like that.

I mostly just use these apps for work:

* A terminal. I use iTerm2 for this. I usually uses tmux to manage "windows" in terminal so I usually don't open multiple iTerm2 windows.
* IDE. Usually Intellij Idea or other JetBrain family products.
* Browser: Firefox.
* Team collaboration software like Slack and Zoom.

Most of those software are cross platform so I don't have much complain about the software themselves. The things I want to change are on the desktop environment itself.

There is a thing in MacOS that I wouldn't be used to in a million years: the logic of windows grouping for the same app. It results these problems:

First, it needs different keyboard shortcut when switch through windows. It just adds unnecessary complexity. Especially with my HHKB keyboard, the `~`/<code>`</code> key is far away from Tab key: it's at the top right corner. And it's hard to see from a glance what windows are available.

Talking about seeing what windows are available, the dock doesn't do a good job as well. You can only see which apps are open. And I don't feel it's doing a good job even for that. Usually I just end with lots of opened windows/apps that's no longer needed and it's hard to keep track of them without a proper panel that shows all the windows.

## Make It More KDE/Windows Like

So my goal here is to make it more KDE/Windows like, which means:

* Use the same keyboard shortcut to cycle through all the windows, do not group the windows by app.
* Have a panel shows all the windows. Again, do not group by app.
* This is a good to have: use keyboard to snap windows on the left/right or maximum.

I don't need the "start menu" since I usually just open apps by bring up the searchable launcher: Spotlight in MacOS and KRunner in KDE.

So here are a list of software that archive my needs:

* [AltTab](https://alt-tab-macos.netlify.app/): cycle through all the windows without grouping by app.
* [Rectangle](https://rectangleapp.com/): Windows snap and keyboard shortcuts
* [uBar](https://ubarapp.com/) or [sidebar](https://sidebarapp.net/): KDE/Windows like panel bar to show all windows.

## Other Quality of Life Improvements

There are two other software I find very useful even though they are not related to the workflow above.

First, [noTunes](https://github.com/tombonez/noTunes). It bans the start of Apple Music. I find it's very annoying that when I accidentally pressed some button or touched my Airpods, the Apple Music popped up. I don't even know what triggered it. So this software solves this problem perfectly.

The second one is [Karabiner-Elements](https://karabiner-elements.pqrs.org/). This is a very powerful custom key mapping software. But I mainly use it to support two keyboards at the same time. That is a very very niche personal need: I use two same keyboards as split keyboard. I can write more on that in the future blogs. But the point is, MacOS doesn't support two keyboards at the same time very well and this software solves that.

