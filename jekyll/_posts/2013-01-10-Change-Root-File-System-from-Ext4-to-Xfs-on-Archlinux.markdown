---
layout: post
title: Change Root File System from Ext4 to Xfs on Archlinux
tags: [ext4, xfs, initramfs, backup]
index: ['/Computer Science/Storage']
---

[Xfs](http://xfs.org/) is said a high performance file system. So I changed my root file system from ext4 to xfs today. There are some tricky things. This article is a simple HOWTO.

## 1. Make a xfs partition

Note: a xfs partition cannot be shrunk. And if you don't have a split boot partition, you should make one with `ext2` since grub doesn't support xfs well.

## 2. Copy files with tar

Copy files from original ext4 partition to the new xfs partition with `tar`. Be careful, *never* use `cp` to backup a file system since `cp` will change the setuid bit and follow hard links. Use `tar` instead, for example:

	tar --exclude=proc/ --exclude=sys/ -cf - / | ( cd <xfs_mount_path> ; tar -xpvf - )

I just exclude two directories for an example. You should exclude all mounted file systems (except root of course). It is recommended to use a bootable CD or USB disk to boot your system, mount both ext4 and xfs partitions, then use `tar` to copy files. In this way, you need not exclude any directory:

	tar  -cf - <ext4_mount_path> | ( cd <xfs_mount_path> ; tar -xpvf - )

## 3. Config grub and modify fstab

There is nothing special here. Just modify your [grub config](https://wiki.archlinux.org/index.php/GRUB) and [fstab](https://wiki.archlinux.org/index.php/fstab) in your xfs partition. Remember to make an entry for your old root file system while configuring the grub. Then you could try to reboot. If the system could not mount xfs partition, you may want to boot with the old ext4 root partition and modify initramfs for xfs support as below.

## 4. Modify initramfs to include xfs support

In the boot process, kernel uses initramfs as root first, then mounts the real root file system. If your initramfs doesn't include support for xfs, you may see these errors during booting:

	fsck.xfs: file not found
	mount: unknown filesystem type "xfs"

In this case, you need to boot into your old ext4 root partition and modify initramfs. In archlinux, you can do it with [mkinitcpio](https://wiki.archlinux.org/index.php/mkinitcpio).

Just add `xfs` to kernel modules and `fsck.xfs` to binary files in `/etc/mkinitcpio.conf`: 

	MODULES="xfs"
	BINARIES="fsck.xfs"

Remember to copy it to your xfs partition, too.

Then make an initramfs:

	mkinitcpio -p linux


