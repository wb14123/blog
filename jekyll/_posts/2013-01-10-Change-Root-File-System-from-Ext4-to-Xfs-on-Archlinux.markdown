---
layout: post
title: Change Root File System from Ext4 to Xfs on Archlinux
tags: [ext4, xfs, initramfs, backup]
---

[Xfs](http://xfs.org/) is said a high performance file system. So I changed my root file system from ext4 to xfs today. There are some tricky things. This article is a simple HOWTO.

## 1. Make a xfs partation

Note: a xfs partation cannot be shrunk. And if you don't have a splite boot partation, you should make one with `ext2` since grub don't support xfs well.

## 2. Copy files with tar

Copy files from origin ext4 partation to the new xfs partation with `tar`. Be careful, *never* use `cp` to backup a file system since `cp` will change the setuid bit and follow hard links. Use `tar` instead, for example:

	tar --exclude=proc/ --exclude=sys/ -cf - / | ( cd <xfs_mount_path> ; tar -xpvf - )

I just exclude two directories for an example. You should exclude all mounted file systems (except root of cause). It is recommended to use a bootable CD or USB disk to boot your system, mount both ext4 and xfs partations, then use `tar` to copy files. In this way, you need not exclude any directory:

	tar  -cf - <ext4_mount_path> | ( cd <xfs_mount_path> ; tar -xpvf - )

## 3. Config grub and modify fstab

There is nothing special here. Just modify your [grub config](https://wiki.archlinux.org/index.php/GRUB) and [fstab](https://wiki.archlinux.org/index.php/fstab) in your xfs partation. Remember to make an entry for your old root file system while config the grub. Then you could try to reboot. If the system could not mount xfs partation, you may want to boot with the old ext4 root partation and modify initramfs for xfs support as bellow.

## 4. Modify initramfs to include xfs support

In the boot process, kernel use initramfs as root first, then mount the real root file system. If your initramfs don't include support for xfs, you may see these errors during booting:

	fsck.xfs: file not found
	mount: unknown filesystem type "xfs"

In this case,  you need boot into your old ext4 root partation and modify initramfs. In archlinux, you can do it with [mkinitcpio](https://wiki.archlinux.org/index.php/mkinitcpio).

Just add `xfs` to kernel modules and `fsck.xfs` to binary files in `/etc/mkinitcpio.conf`: 

	MODULES="xfs"
	BINARIES="fsck.xfs"

Remember to copy it to your xfs partation, too.

Then make a initramfs:

	mkinitcpio -p linux


