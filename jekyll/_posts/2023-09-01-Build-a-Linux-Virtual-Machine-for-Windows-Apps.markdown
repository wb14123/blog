---
layout: post
title: Build a Linux Virtual Machine for Windows Apps
tags: [Linux, Windows, Virtual Machine, Deepin, Wine]
index: ['/Computer Science/Operating System/Linux']
---

## Background

Linux is great. However sometimes you just need to run some Windows only applications to collaborate with other people, especially if it's impossible to let the other party to change the software. Luckily I rarely run into that situation in the past 10+ years. The only recent exceptions I can remember are filling some government forms (which uses pdf with XFA form. Yes Firefox can fill that now but it's still incompatible with Adobe Reader from time to time), use IM and video meeting software with a previous Chinese client for some consultant work.

Even it's rarely used, it's handy to have a VM to run Windows applications. But Windows is becoming more and more bloat, adding more and more tracking and ads, basically more holistic to users. The only good parts in Windows XP and Windows 7 have long gone. Currently I have a Windows 10 VM, but I'm not sure if I ever want to login to Windows 11 if Windows 10's life is end. So it's good to have a backup plan to run Windows apps without Windows. Practise reasons aside, it's just fun to play with Linux distros. Linux and its desktop environments are so diverse and configurable, I spent such a great time to explore what are the possibilities.


There is [Wine](https://www.winehq.org/) to run Windows applications on Linux. It's not perfect. Some apps need to be tweaked a lot in order to run with wine and some are just nearly impossible. Even the software can be run with wine, I don't want to run it on my OS since wine is just a compatibility layer, not a sandbox. Which means the typical malware like behaviours in Windows applications are still effective under Wine. So I need to run it in a virtual machine. It also gives us an opportunity to select a distro to focus more on this specific task.


I have tried a few distros in the last few days. At the end I find [Deepin Linux](https://www.deepin.org) is the best one for this use case. Especially you want to run Chinese Windows apps.

## A Little History of Deepin

Deepin's root is in Windows. It first started as a Windows online forum and then started to customize and piracy Windows XP. The year was 2006. Almost no one bought Windows in China back in the days. I don't know if business or even Universities ever bought Windows licenses or not, but even they do, it's a very common practise to install piracy Windows in those environment because the popular ones are so user friendly. Computer sellers would ask the buyers if they want to change the stock Windows to a piracy one, and most of the time they do. Ironically, the only time I've inputted a (legitimately obtained) Windows key is when I was working at Redhat and setting up a Windows server for testing Samba and nfs. The popular piracy versions are really impressive: the installation is fast and easy, they are more beautiful, they include things like system backup and recovery, they have common drivers pre-installed and application to find drivers (not like the driver finder on Windows, this one actually works), the system was cut down to a very small size and so on. However, if it sounds sketchy to you, you are not wrong. Even though the user experience maybe superior, there is no shortage of back doors and things like that. The popular versions even have their own more sketchy piracy versions. However, that era is wild west for computer security and just one more vulnerability didn't really matter that much in my mind.

Anyway, Deepin was one of the most popular among them. But things didn't last for long. Around 2008, the year China hosted its first Olympic Game, the person behind a popular piracy version got arrested. Even the common practise of using piracy Windows in China lasted a long time after that, the big ones felt the risk and stopped making them. Some of them started to make Linux distros instead. Again, Deepin became one of the most popular. [雨林沐风 (YLMF)](https://en.wikipedia.org/wiki/YLMF_Computer_Technology_Co.,_Ltd.) is another very popular one which is in famous of its clean and beautify theme in the Windows piracy era. It started to make Linux distro ([YLMF OS](https://en.wikipedia.org/wiki/StartOS)) around the same time. It is the first Linux distro I've ever used and introduced the whole world of Linux to me.

It's no wonder Deepin Linux has good support for Windows applications: Windows users are its earliest user base. It's still true nowadays: even with some failed attempts, Chinese government never stopped exploring to use Linux instead of Windows on government devices. After these years, because of factors like applications are more web based instead of native, and the better experience of Linux desktop, Chinese government actually replaced a large amount of their devices with Linux. From what I know, they are using [Ubuntu Kylin](https://en.wikipedia.org/wiki/Ubuntu_Kylin) instead of Deepin or UOS (commercial version of Deepin), but the market is large enough to motivate Deepin to continue maintaining Windows app supports.

## Deepin Linux 101

We've talked enough history. Let's look at Deepin Linux at nowadays. It has it's own desktop environment called DDE and includes lots of its own apps like browser, video player, mail client and so on. But it's not my taste and the DE is pretty resource hungry on my machine. Luckily, Deepin is based on Debian stable, so you can basically customize to whatever you like using Debian packages, which we will do later.

The main thing we want in Deepin Linux is its app store. It has lots of Windows applications supported by default. Deepin actually has its own wine version deepin-wine to support those Windows apps better. No matter what tweaks we are going to do with the system, make sure app store works after that.

It also ships with Android support with UEngine, which is a fork of Anbox. There are some officially supported Android apps in the package repo but seems they are not findable in app store. You can use `apt search uengine` to find them in terminal. I've never had good experience with Android in VM and this time it's no exception: I tried to install an app from apt and it couldn't start because of uengine startup timeout. I'm not sure if it will be better on a physical machine but I don't bother to try it.

Overall, I have double feelings about Deepin. On one hand, it's pretty impressive on the technical side about what they have done and the community they've built. On the other hand, it always feels a little bit sketchy. Even after the Windows piracy era, the Linux distro is still less trustworthy in my mind because of some telemetry its app store collects, not straightforward removable stock apps, the connection (or the intention to connect) with Chinese government and so on. In addition of all the Windows apps I'm going to install, I will not have any personal or important data on it.

After understanding the basics of Deepin Linux, let's go ahead to install and tweak it. To have a taste of what it will look like, let me show a screenshot of my setup:


![screnshot](/static/images/2023-08-31-Build-a-Linux-VM-for-Windows-Apps/screenshot.png)

## Installation

The installation is pretty straightforward, but make sure to make these tweaks:

* During the installation, it will detect that you are in a VM and prompt to use "performance" mode. Make sure to select it so that it will be faster. Even though we will replace the DE later so I don't think it matters that much, but it doesn't hurt anyway.
* By default it will create a recovery partition, which is a waste of storage since we are using a VM. We can take snapshots through the VM software if we backup and recovery. So make sure to manually partition the file system and not using recovery partition.

## Replace DDE with XFCE

As I said, I don't like the default DDE Deepin ships. And with limited time I don't find it's very configurable as well. So we will replace it with the less resource hungry and highly customizable XFCE. In order to install XFCE, run this in the terminal:

```
sudo apt install xfce4 xfce4-goodies
```

Then logout and select `xfce` in the login screen.

## Configure lightdm

Deepin is using `lightdm` as its display manager. To match our simple xfce feeling, I'd like to change the login screen to a simpler and reto look. Open `/etc/ligthdm/lightdm.conf` and apply these changes:

```
- greeter-session=lightdm-deepin-greeter
+ greeter-session=lightdm-gtk-greeter
- user-session=deepin
+ user-sesion=xfce
```

## Install Windows 95 theme

Another important reason of choosing XFCE the [Chicago95 theme](https://github.com/grassmunk/Chicago95), which makes XFCE looks like Windows 95. If we are having an OS for running Windows programs, what's a better look than Windows 95/98/2000 era theme?

Go to [Chicago95's Github repo](https://github.com/grassmunk/Chicago95) and follow the instructions to install it. Since it's made for XUbuntu, which is based on Ubuntu which is in turn based on Debian, the installation script works perfectly. After installation, read the popped up txt file so tweak remaining things to make it more Windows 95 like if you want.

If you also want to make the login screen Windows 95 like, you need to use `lightdm-webkit-greeter` instead of the `lightdm-gtk-greeter` above and change the theme. See [the doc](https://github.com/grassmunk/Chicago95/tree/5670fde8ce33b33d37622b888278aa9cdbe5eea2/Lightdm/Chicago95) for more details. However, `lightdm-webkit-greeter` is not in Debian or Deepin's package repo by default and I find the trouble to install it manually doesn't worth it, so I didn't make the change.

For Firefox, there is a [Windows 98 SE](https://addons.mozilla.org/en-US/firefox/addon/windows-98-se/?utm_source=addons.mozilla.org&utm_medium=referral&utm_content=search) theme I find fit into the system theme the best.

## Other XFCE tweaks

* Disable compositor in Settings -> Window Manager Tweaks -> Compositor. Some windows will have a black frame after this, so enable/disable it based on your preference.
* I'd also like to disable auto session save:
	* Uncheck all the boxes in Settings -> Session and Startup -> General (I find it doesn't prompt and auto save if `prompt on logout` is checked)
	* Remove all the existing sessions: `rm -r ~/.cache/sessions/*`


## Remove Stock Deepin Apps

There are lots of stock apps made by Deepin. Even though I like the effort, I still prefer the familiar ones and the ones XFCE has. So I need to uninstall the stock apps. However, you cannot uninstall them through Deepin's app store, so we need to use `apt` to find and remove them.

Use `apt search deepin | grep installed` to find installed deepin packages and remove the ones you don't want. Then use `sudo apt autoremove` and `sudo apt autoclean` to cleanup the not needed dependencies. Make sure app store is still working after this since it's the whole point of using Deepin.

## Disable Deepin Services

I didn't remove all the deepin related packages since some of them are needed by the App Store. However, I don't think some of them are needed to run as deamon even if I left them on the machine. So type `systemctl status deepin-` and press tab for autocomplete to see the systemd services related to deepin, and use `systemctl disable <service>` to disable the ones you don't need.
