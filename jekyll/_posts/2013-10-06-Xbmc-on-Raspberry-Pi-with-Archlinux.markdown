---
layout: post
title: Xbmc on Raspberry Pi with Archlinux
tags: [xbmc, raspberry pi]
---

前段时间买了Raspberry Pi。一开始安装的系统是raspbian，但是感觉安装软件太不方便了，于是安装了Archlinux Arm。在Archlinux上安装xbmc上很简单。这里简单介绍一下。

硬件
----------------------------

我使用的是很老的电视，没有HDMI接口的。用到的硬件主要有：

* Raspberry Pi
* A/V线
* Audio，音频转莲花头线
* 电源。最好使用5V/1A的。我使用的是有源usb hub提供的电源，[力特（Z－TEK）ZK033A](http://item.jd.com/648176.html)

我还使用了无线网卡。主要是我住的地方只能使用无线。我用的无线网卡是

* [EDUP EP-N8508GS黄金版](http://www.amazon.cn/gp/product/B007FN9D80/ref=oh_details_o00_s00_i00?ie=UTF8&psc=1)。


软件
-----------------------------

使用`wifi-menu`可以在命令行中方便的连接无线网络。这个软件是默认安装的。

Pacman可以安装的：

* `xbmc-rbp`
* 中文字体，`wqy-micoryhei`, `wqy-zenhei`
* `yaourt`，用以安装aur源
* 解码器，详见[wiki](https://wiki.archlinux.org/index.php/codecs)

一些xbmc的插件，使用aur源安装。

* `xbmc-addons-chinese-svn`，一些中文的在线视频和音乐
* `xbmc-addon-repo-hdpfans`，中文的插件源
* `xbmcswifti`

不需要安装图形环境就可以使用xbmc，只需要执行`xbmc-standalone`

中文化
------------------------------

设置语言为中文，字体为Arial即可。


Trouble Shooting
-------------------------------

### 1. 在电视中显示为黑白

试着在`/boot/config.txt`中设定`sdtv_mode`的值为2或3。

### 2. 在启动xbmc时提示`failed to open vchiq instance`

```
chmod a+wr /dev/vchiq
```

### 3. 在空闲时cpu占用率过高

在xbmc中关闭rss feed。
