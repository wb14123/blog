---
layout: post
title: Build a Unix Like Environment on Windows
tags: [Windows, Python, bash]
index: ['/Computer Science/Operating System/Windows']
---

I've bought a Surface Pro 4 some days ago. It is very amazing and I'd like to use it as my backup development laptop. My daily development is under Linux and Mac OS X. I use terminal and lots of bash scripts everyday. So I need a Unix-like environment on Windows. This article will introduce how to do that.

Terminal and Unix Tools
-------------

There is a famous software called [Cygwin](https://www.cygwin.com/) which provides many unix tools along with a terminal. You can download it from its homepage and install it with GUI.

While installing it, it will ask you which tools you'd like to install. Just install the default ones and Lynx is enough, since we will install a package manager and it will be easier to install other tools then.

You can use Xterm with Cygwin terminal, so you can configure it as you are in Linux.


Package Manager
------------

The most missed thing while I'm using Windows is Linux's package manager. You can search, install, update and manage software very easily with it. There is also HomeBrew under Mac OS X so I'd like something like that under Windows. I searched on Google and found [apt-cyg](https://github.com/transcode-open/apt-cyg) which can manage packages in Cygwin. You can follow the steps on its homepage to install it. After installing it, you can install wget with it so that it will stop printing warning messages.

I've installed tmux, zsh, Git and vim with it. And configure them with my [config files](https://github.com/wb14123/dotfiles). I only need to change the tmux start up config:

```
- set -g default-command "reattach-to-user-namespace -l /bin/zsh"
+ set -g default-command "/usr/bin/zsh"
```

Except this, everything else works very well without any problem.


Python
----------

There are many tools that are written in Python. And my work also uses Python a lot. You can install Python with apt-cyg. But there will be some tricky things if you need to install some Python packages with pip.

First we will install pip:

```
apt-cyg install python python-devel
wget http://peak.telecommunity.com/dist/ez_setup.py
python ez_setup.py
easy_install pip
```

Then we need to install gcc in order to compile some python packages:

```
apt-cyg install colorgcc gcc-core gcc-g++ libgcc1
```

Then we need to change a python header file: /usr/include/python2.7/pyconfig.h:

```
- #define __BSD_VISIBLE 1
+ #define __BSD_VISIBLE 0
```
