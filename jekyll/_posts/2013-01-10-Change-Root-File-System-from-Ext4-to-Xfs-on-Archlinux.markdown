
---
layout: post
title: Change Root File System from Ext4 to Xfs on Archlinux
tags: [ext4, xfs, initramfs, backup]
---

[Xfs](http://xfs.org/) is said a high performance file system. So I changed my root file system from ext4 to xfs today. There are some tricky things. This article is a simple HOWTO.

## 1. Make a partation of xfs and mount it

Note: A xfs partation cannot be shrunk

## 2. Copy files with tar

Copy the files from origin ext4 root file system to the new xfs file system with `tar`. Be careful, *never* use `cp` to backup a file system since `cp` will change the setuid bit and follow hard links. Use `tar` instead, for example:

	tar --exclude=proc/ --exclude=sys/ -cf - / | ( cd /mnt ; tar -xpvf - )

I just exclude two directories for an example. You should exclude all mounted file systems (except root of cause). It is recommended to use a bootable CD or USB disk to boot your system, mount the origin ext4 root partation and the new xfs partation then use `tar` to copy files.

## 3. Modify initramfs to include xfs support

In the boot process, kernel use initramfs as root the file system at first and then mount the real root file system. My initramfs only include support for ext2/3/4. So I need to modify it. In archlinux, you can do it with [mkinitcpio](https://wiki.archlinux.org/index.php/mkinitcpio).

Just add xfs for kernel modules and binary files in file `/etc/mkinitcpio.conf`:

	MODULES="xfs"
	BINARIES="fsck.xfs"

And then make a initramfs:

	mkinitcpio -p linux

## 4. Modify grub config and fstab

There is nothing special here. Just modify your [grub config](https://wiki.archlinux.org/index.php/GRUB) and [fstab](https://wiki.archlinux.org/index.php/fstab) to adapt the new partation.
