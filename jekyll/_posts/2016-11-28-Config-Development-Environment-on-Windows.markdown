---
layout: post
title: Build a Unix Like Environment on Windows
tags: [Windows, Python, bash]
---

I've bought a Surface Pro 4 some days ago. It is very amazing and I'd like to use it as my backup development laptop. My daily development is under Linux and Mac OS X. I use terminal and lots of bash scripts everyday. So I need a Unix-like environment on Windows. This article will introduce how to do that.

Terminal and Unix Tools
-------------

There is a famous software called [Cygwin](https://www.cygwin.com/) which provides many unix tools along with a terminal. Download it from its homepage and then you can install it with GUI.

While installing it, it will ask you which tools you'd like to install. Just install the default ones and Lynx is enough for now, since we will install a package manager so it will be easy to install other tools then.

You can use Xterm with Cygwin terminal, so you can config it as you are in Linux.


Package Manager
------------

The most missed thing while I'm using Windows is Linux's package manager. You can search, install, update and manage software very easily with it. There is also HomeBrew under Mac OS X and I'd like something like that under Windows. So I've found [apt-cyg](https://github.com/transcode-open/apt-cyg) which can manage packages in Cygwin. You can follow the steps on its homepage to install it. After install it, you can install wget with it so that it will stop print warning messages.

I've installed tmux, zsh, Git and vim with it. And config them with my [config files](https://github.com/wb14123/dotfiles). I only need to change the tmux start up config:

```
- set -g default-command "reattach-to-user-namespace -l /bin/zsh"
+ set -g default-command "/usr/bin/zsh"
```

Except this, everything else works very well without any problem.


Python
----------

There are many tools are written in Python. And my work also uses Python a lot. You can install Python with apt-cyg. But there will be some tricky things if you need to install some Python packages with pip.

First we will install pip:

```
apt-cyg install python python-devel
wget http://peak.telecommunity.com/dist/ez_setup.py
python ez_setup.py
easy_install pip
```

In order to install packages that need to compile, first we need to install packages related to gcc:

```
apt-cyg searchall gcc
```

Then we need to change a python header file: :

```
```
