---
layout: post
title: Comparison Between Linux Desktop Environments
tags: [Openbox, Xfce4, LXDE, Gnome, KDE, UI]
index: ['/Computer Science/Operating System/Linux']
---

I finally chose KDE after using lots of desktop environments and window managers for many years. I think it is worth writing a summary of them.

## Window Managers: [Openbox](http://openbox.org/) And [Awesome](http://awesome.naquadah.org/)

If you want the simplest GUI environment, you just need a window manager. If you want more, you can install a panel, a file manager to take care of your desktop (or just use feh to set a wallpaper).

The bright side of using a window manager combined with little programs is:

* It is fast.
* You feel like you can control everything.
* It is cool.

I have used openbox for a long time until I have installed Archlinux on a Macbook Pro. I could hardly find any performance difference between openbox and Gnome 3 after that. Desktop environment is not the biggest factor affecting performance. Think about Chrome, Firefox, Eclipse and so on. While you upgrade your RAM for these applications, you can use a more advanced desktop environment at the same time.

The truth about you can control everything is, you **have to** control them. You can configure other environments easily, too. Including the shortcuts, autostart programs and so on. (Except for Gnome, we will talk about it later).

It is cool about tiling window managers such as awesome. But it really makes a person crazy to use so many shortcuts. For example, I use shortcuts in chrome, vim, terminal, tmux, desktop environment and so on. I don't think there are so many easy-to-press shortcuts. When you want tiling, you could use tmux or screen in your terminal, or use Gnome and KDE to tile windows.

And the downside of them is:

* Difficult to configure unless you are skillful.
* Difficult to make it beautiful unless you have artistic talent.
* You need to install lots of programs to provide some useful feature and make it not so ugly. After that, you will find you have installed lots of libraries from other desktop environment.

## Simple Desktop: [Xfce 4](http://xfce.org/) And [LXDE](http://lxde.org/)

LXDE is more like a pre-configured openbox environment while xfce4 is like a simpler version of Gnome (or KDE) and comes with lots of useful panel utils. They are a good trade-off between performance and completeness. I recommend using them on your old PC.

## Complete Environment: Gnome 3, [MATE](http://mate-desktop.org/), [Cinnamon](http://cinnamon.linuxmint.com/) and KDE

It is a famous holy war between Gnome and KDE. I will talk about Gnome first.

I preferred Gnome in earlier times because KDE "looks like Windows". Gnome 2 is my first desktop environment and I love it. When Ubuntu used Unity instead of it, I changed to Archlinux with Gnome 3. After a long time, I found MATE(a fork of Gnome 2) and Cinnamon(a fork of Gnome 3). The problem with Gnome 3 is it tries to take care of everything but it is not complete enough. MATE is as comfortable as Gnome 2 but it feels not so "modern". Cinnamon is also good and I used it for a long time. Gnome 3 may not be as good as people expect but it has a good approach. **The biggest problem of Gnome is it uses dconfig!** How could it just use a binary format to store configurations. Why can't I just grep a setting but can only click and find it? It is the most silly thing in the non-Windows world.

So I decided to give KDE a try. I surprisingly found it provides a more complete environment than Gnome. It also tries to take over all things but it does a good job. And it is really more beautiful and comes out with more good themes. I feel happy with it now.

## Also Good to Try: [Enlightenment(E17)](http://enlightenment.org)

I was amazed when I heard E17 had been developed for 12 years. And it finally released. I tried the developing E17 on archlinux and it seems good. But I did not use it for a long time. You can try it yourself.
