---
layout: post
title: A Review of Linux on Surface Pro 4
tags: [Linux, Surface, Microsoft, Operating system, tech]
index: [/Computer Science/Operating System/Linux]
---

## Background

I bought a Surface Pro 4 at 2016. It has an Intel Core m3-6Y30 CPU and 4GB memory. The spec is not that impressive even compared to an average laptop released years earlier. On the other hand, the form factor is very attractive to me: at a very low price, you get a tablet with a beautiful HiDPI 2k screen, a pressure sensitive stylus and an useable keyboard. It is on the heavier side if used as a tablet, but compared to other laptops, it's very light. It served me very well for my limited use cases. The blog [Build a Unix Like Environment on Windows](/2016-11-28-Config-Development-Environment-on-Windows.html) was written at that era. Some years later, I bought a more powerful laptop when I needed to work while traveling. So I gave the Surface away to a family member.

However, during the past years, I couldn't stop thinking about having a Linux tablet. At first I checked [Pinetab](https://pine64.org/devices/pinetab/), then I realized I had a Surface which would be perfect if I could install Linux on it. I searched online and found some successful stories. So when I [travelled back to my hometown](/2024-03-19-Travel-Back-to-China.html) at the beginning of this year, I brought the Surface back with me and started to experiment with it.

## Use Cases

Before I go further, I need to mention my intended use cases:

* Browse Internet. Mainly [RSS Brain](https://www.rssbrain.com/), the RSS reader I built by myself.
* Media consumption: watch videos from my Samba share and online websites like Youtube.
* PDF reading: reading only is enough for me but it's better if I can take notes in the PDF.
* Sketches: I don't have a habit to do handwriting notes even at students era. Nowadays it's more efficient and readable/searchable to take text notes with Markdown. However, I do like drawing sketches on paper when brain storming or resolving some hard problem. Moving it to digital has a lot of benefits if it works.
* Drawing: this is a good to have feature. I don't really have needs to draw things but it's always fun. Especially with the development of AI, if I draw something and send it to a more powerful machine to generate images, it could open doors to many possibilities.

## Installation

The installation of Linux is actually very easy. I tried two distros and the installation process went very smooth for both of them. The distros I tried are [EndeavourOS](https://endeavouros.com/) and Fedora workstation 40.

The installation steps are well documented in [linux-surface's wiki](https://github.com/linux-surface/linux-surface/wiki/Installation-and-Setup#installation). [linux-surface](https://github.com/linux-surface/linux-surface) is the Linux kernel and tools for Surface devices. The wiki page has its installation steps as well.

In general, if only used as a laptop, the experience is almost perfect even without the linux-surface kernel. But using it as a tablet is another story.

## What Works

Let's talk about what works first. Even without linux-surface kernel, almost everything works except touch screen and stylus. That includes things like wireless network, bluetooth, keyboard, power profile, UI scaling for Hi-DPI and so on. Multi touch and pressure sensitive stylus works as well (sort of, see sections below) after installed linux-surface kernel. Battery life is good enough: about 5-6 hours of light usage like web browsing, PDF reading, and about 3 hours of video watching. (Just some estimated time from my experience, no serious benchmarking was done).

On the software side, automatic screen rotation is enabled on both distros I tried. KDE with EndeavourOS is very fast and responsive. When the keyboard is detached, it enters tablet mode which makes some UI larger and more user friendly with touch gestures. For example, you can just touch on a folder to open it in Dolphin instead of double click it.

For Gnome, it's less responsive than KDE but the UI is really beautiful when used as a tablet. I was never a fan after Gnome 3 but I guess the UI changes it made makes more sense on a tablet than on a laptop or a desktop. The overall layout really reminds you about the iPad or Android tablet (in a good way), but with the power of a real desktop OS at the same time. I would really like it if it uses less resource.

Even though the overall experience is positive and has the potential to meet all my use cases, one serious problem made it very unusable and made me gave up Linux on Surface at the end.

## The Problems in Both Distros

The deal breaker problem is touch recognition. The problem is in the surface-linux tools so it affects all the distros. The biggest problem is ghost touch: touches are registered randomly even when I do nothing. I tried a lot of workarounds including the ones mentioned in [linux-surface's wiki page](https://github.com/linux-surface/linux-surface/wiki/Surface-Pro-5), but none of them actually resolved it completely. Sometimes it's fixed after reboot but reappeared after next reboot. Sometimes it get fixed for a period of time but reappeared after a system upgrade. Sometimes the touch screen doesn't work at all after resume from sleep. The randomness and the serious of the problem is really annoying so I gave up using it with Linux at last.

Other than the ghost touching, another big problem about touch recognition is palm rejection. It's really annoying when draw things with the pen. In iptsd (surface-linux's deamon for touch recognition), there is a configuration to disable touch screen when using a pen but it doesn't work well. So it makes drawing very unusable.

Both KDE and Gnome has virtual keyboards when the physical keyboard is detached, and works most of the time despite the problems I'll mention in the following sections. But if you have setup disk encryption with a password, there is no virtual keyboard when you input the disk password, so a physical keyboard is always needed during the boot. Which can be annoying but not really a deal breaker.

The last big problem is battery drain during sleep. It uses about 30% battery for one night even it has been put into sleep. I had similar issues for other laptops. I believe there maybe some configurations I can tune to fix that. But after I gave up Linux on it because of the ghost touch, I didn't dig deeper into that.

Other than the problems shared by both distros, each distro/desktop environment also has their own problems.

## The Problems in KDE with EndeavourOS

The biggest problem in KDE other than the ones I talked above, is the virtual keyboard. It's buggy and not very stable. Sometimes it kept pop up and sometimes it doesn't show up. It's annoying especially at the login screen: if it's not popped up you will still need a physical keyboard, which prevent it to be a real tablet. Sometimes when the keyboard is popped up, the panel at the bottom cannot be touched. The bugs happened randomly that makes it hard to be properly reported.

Another problem is the touch gesture for right click. Naturally, with a touch screen, long press should be treated like a right click. But that is not the case for KDE. So a lot of operations just cannot be done without a mouse when you need a right click.

Resize a window is also very tricky with touch only operation: you need to touch on the boarder precisely on the first try.

At last, the scroll behaviour is not very smooth. It makes me a little bit dizzy just by scrolling through web pages and PDFs.

So I thought give another distro and desktop environment a try, to see if they can resolve my problems.

## The Problems in Gnome with Fedora Workstation 40

I choose Fedora because it comes with Gnome, and I had good experience with it before. After the installation, the first impression is it's much slower than KDE with EndeavourOS. I found it enables swap and ZRam by default so I disabled them. It's better but still slower than KDE. It uses more memory at around 40-50% percentage while idel. And I got a lot of OOM kills which almost never happened with KDE on EndeavourOS.

Maybe because of the slowness, it's also buggy for lots of operations. For example, when switch to the workspace view from PDF viewer with 4 fingers swipe up, the PDF keeps scrolling at the background. And when scroll in the file manager, the context menu keeps popping up.

Other than the slowness, there is a problem on the virtual keyboard as well: the backspace key doesn't work properly. I found a workaround by install a third-party Gnome addon, but sometimes the old keyboard still popped up.

## Go Back to Windows 10

I'd say if the touch recognition works well enough, all the other problems are acceptable with KDE. But with those problems, I finally decided to fallback to Windows 10 again. It works well enough, just as I remembered from years ago. However I abandoned OneNotes and some other Microsoft products and use the following software instead:

* Firefox as the browser.
* Nextcloud to sync the files.
* Samba for video sharing.
* Built in video player for local video playing.
* Krita for drawing and sketches.
* Drawboard PDF for PDF reading.

It's pretty disappointing that this device cannot be used with Linux properly. But using Windows is still better to just let the device sitting there doing nothing. Maybe I will re-evaluate it after Windows 10 is end of life next year.

