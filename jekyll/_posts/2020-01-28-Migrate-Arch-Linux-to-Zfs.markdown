---
layout: post
title: Migrate Arch Linux to ZFS
tags: [linux, zfs, technology, freebsd]
---

I migrated my Arch Linux installation to ZFS recently. This article describes why and how I did that.


## Why ZFS?

ZFS is a very advanced file system that has many handful features. I first knew it many years ago, when I installed FreeNAS for fun. FreeNAS is an OS made for NAS based on FreeBSD. When I played with it, the features of ZFS really shocked me and let me realised what a file system can really do. I will list the features of ZFS that I'm using and not usually avaliable in traditional file systems:

* Snapshot: It is really easy to take a snapshot. Because ZFS has copy-on-write feature, so a snapshot doesn't take any space until you change some content on the file system. It makes backup way much cheaper and easier. This is the biggest feature that let me want to migrate to ZFS.
* RAIDZ: The traditional raid hides the disks from the file system. But for ZFS, you can use raidz which the file system can see the topological of disks so that it can optimize the I/O based on the information.
* Dataset: You can create many datasets in a zfs pool. Which can have different configurations based on the use case.
* L2 Cache: You can use hard disk drives as the file system and add SDD as read or write cache. Which is a very good balance between the storage space, speed and the price.
* Send/Recv: ZFS has commands to send the datasets and snapshots to another dataset or a remote machine. Which makes the offline backup much easier and cheaper.
* Compression: By compression you can save both storage space and make the read/write faster. And you can set different compression algorithm for different use cases.

But back then ZFS is not well supported on Linux so I used ext4. Then I had an internship at Redhat and realised ext4 was really an old file system so I migrated to XFS. Until now, my storage configuration is one 128G SSD for the system (root partition). Two 2TB HDDs as a RAID1 array to store all the other data. I backup the system from SDD to HDD regularly in case of the SDD fails. And I don't have any way to protect me from deleting files by mistake.

## Why not FreeBSD?

It is very nature to use BSD with ZFS since it is supported by deafult. With linux, you must install another kernel module which is not native supported by kernel. And Linus just said "don't use ZFS" somedays ago. So I was seriously thinking about change my OS to FreeBSD. Compared to Arch Linux, the system is more security because all the packages are native supported by the FreeBSD developer and the port system also has a lot of softwares. But finally something stops me from migrating to FreeBSD:

* The game support. Last year, game on Linux finally breaks out because of the Poton on Steam. Though I don't play games a lot, it's still a pity to have no game to play.
* FreeBSD starts using ZFS on Linux as the upstream of ZFS. So with the bigger community, ZFS on Linux may has better support and more features in the future.
* It's a risk to migrate to a new system. Arch Linux works great for me for a long time. If the new installation doesn't work, it's time consuming to recover the system. And it says FreeBSD doesn't have a good support on Nvidia graphic card, which I'm currently using.
* FreeBSD's Jails are much mature than Containers on Linux. And I really hate Docker for its deamon running as root. Docker also has bad community reputation (e.g. changing its community version's name). Rkt from Core OS is much better but both Core OS and Rkt are dead after the acquire of Redhat. But finally I found Podman. So I can compromise at this point. Containers also has bigger community. For example, the Nextcloud I'm using has official image so I don't need to build my own one.

## How to Migrate to ZFS?

### 1. Backup the Data

My disks and partitions are like this before the migration:

```
- SDD
  + / (root)
  + /boot
- RAID1 (hdd1, hdd2)
  + /data
```

I only backup my data disks because I will not use my system disk for the ZFS pool at the creation time. I will use it as cache which can be added later. An important note is to **use `tar` or `rsync` to backup data**. Don't use `cp`, it will break the file permissions and links.

### 2. Install ZFS Kernel Modules and Utils

Follow the [Arch Linux's ZFS wiki](https://wiki.archlinux.org/index.php/ZFS#Installation) to do that. I also enabled the [ArchZFS repo](https://wiki.archlinux.org/index.php/Unofficial_user_repositories#archzfs) so I don't need to build it from AUR. This repo is signed so it is more secure.

### 3. Create ZFS Pool and Dataset

I added another HDD for the ZFS pool in order to make a raidz1 pool (like raid5), which doubles the current storage space with raid1. Then create a dataset for the new root partition. The dataset has native encryption and compression enabled.

### 4. Mount the Root Dataset and Copy the Root Partition

Temparory mount the root dataset to `/mnt`. Then follow the Arch Linux wiki about [installing from a host running Arch Linux](https://wiki.archlinux.org/index.php/Install_Arch_Linux_from_existing_Linux#From_a_host_running_Arch_Linux).

### 5. Make New Boot Image and Config Bootloader

I was trying to install boot partition into the ZFS but failed after many attemps. At last I decide just use the old boot partition and sync them to ZFS as backup.

So follow [install Arch Linux on ZFS wiki](https://wiki.archlinux.org/index.php/Install_Arch_Linux_on_ZFS). After the regular installation, add `zfs` in `/etc/mkinitcpio.conf` `HOOKS`:

```
HOOKS=(base udev autodetect modconf block keyboard zfs filesystems)
```

Then using `mkinitcpio -p` to make new boot image. (You may want to backup old images `/boot/initramfs-linux.img` and `/boot/vmlinuz-linux` before this step).

Then in `/boot/grub/grub.cfg`, change the root filesystem to zfs, like this (in the menuentry):

```
linux   /vmlinuz-linux root=ZFS=zroot/root rw  loglevel=3 quiet
```

Which `zroot/root` is the root dataset.

### 6. Config Mount Points

Reconfig the root dataset's mount point to `/` and add the configuration in `/etc/fstab`:

```
zroot/root / zfs defaults,noatime 0 0
```

Don't forget to set the auto import:

```
systemctl enable zfs-import-cache
zpool set cachefile=/etc/zfs/zpool.cache <pool>
systemctl enable zfs-mount
systemctl enable zfs.target
```

Also need to input the passphrase if using encryption. Follow the [Arch Linux's ZFS wiki](https://wiki.archlinux.org/index.php/ZFS) to do that.

### 7. Restart and Config New System

After the reboot, the system should boot into the new root. Then cleanup the old root partition and add it as the cache of the pool: `zpool add -f zroot cache <partition-uuid>`. Then create other datasets and move back all the old data.

Now I can make different datasets for different purpose. For example, make a dataset for nextcloud container's storage. Make a highly compressed dataset for backup files from other machine, and so on.

After the migration, the first boot is slower than before because the root system is migrated to HDD. But the most useful files will be cached into SSD and it feels the same as before after a while. Now I'm very happy with the current setup and super excited about all the advanced features of ZFS! Happy hacking!
