---
layout: post
title: Linux Full Disk Encryption with Yubikey
tags: [Yubikey, Linux, Encryption]
index: ['/Computer Science/Operating System/Linux']
---

## Background

As mentioned in a previous blog [Infrastructure Setup for High Availability](/2023-03-13-Infrastructure-Setup-for-High-Availability.html), I've setup a high available cluster that has 3 machines. But one of them is on my laptop. I feel like I need a dedicated machine for my personal usage, especially I'm planning some travels. So I need to remove the laptop from the cluster. Its disk space is also very limited. With migrating Gluster to Ceph (more blogs to come on that) and not be able to use a disk partition with Ceph's encryption, I need another machine with more disks. So I repurposed another small form factor machine to join the cluster.

I want full disk encryption on it but I don't want to input password every time it boots: this machine is put into a closet and it's very inconvenient to plug/unplug keyboard and monitor. In another blog [Personal ZFS Offsite Backup Solution](/2021-09-19-Personal-ZFS-Offsite-Online-Backup-Solution.html), I talked about a solution to boot encrypted Linxu without input password by setting up TPM. However, old machines only have TPM 1.x chips instead of newer TPM 2.0 chips, which is very tricky to setup and  with very limited support from Linux distros. I don't want to do it again if not necessary. The thread model is also different since this machine is supposed to be at my home all the time. So this time, I found a new solution to use Yubikey to decrypt disks: I just need to keep Yubikey plugged in during the boot process and press it at the proper time. I can also fallback to the password method if there is anything wrong with Yubikey decryption.

There are some great tutorials and wiki pages describe how to do it. I must give the credit to [this article](https://0pointer.net/blog/unlocking-luks2-volumes-with-tpm2-fido2-pkcs11-security-hardware-on-systemd-248.html) that helped me a lot. But all of them are missing some details so I though it would be great to write down my setup so that it may help someone else. My setup is on Arch Linux but the steps should be portable to other Linux distros.

**Warning: the steps may make your system not be able to boot if not setup properly. Make sure to back it up or have a recovery CD available to fix it if things went south.**


## Install Linux with LUKS2

First we need to install Linux with our root partition encrypted. If you are using an installer, most likely there is an option to encryption the disk. If so, select that option and input a passphrase for it. Even though we are using Yubikey to decrypt the disks, it's always good to have a passphrase to decrypt it in case something goes wrong. However, if your threat model needs a solution that doesn't involve a passphrase, I believe you can remove it later after setup Yubikey, though I've never tried it myself.

Some installers will use LUKS1 instead of LUKS2 to encrypt the disk. Don't worry, use `cryptsetup convert --type=LUKS2 <device>` to convert it to a LUKS2 setup after the OS is installed.

Note: do not encrypt boot partition. It usually doesn't have sensitive information and encrypting it doesn't prevent evil maid attack anyway. If you want it to be more secure, considering setup secure boot, which is also mentioned in my previous blog [Personal ZFS Offsite Backup Solution](/2021-09-19-Personal-ZFS-Offsite-Online-Backup-Solution.html).

## Enroll Yubikey to Key Slot

We can enroll a FIDO2 (which is a protocol Yubikey supports) device by using `systemd-cryptenroll`.

Plug in the Yubikey. You can use this command first to list all the FIDO2 devices to make sure the Yubikey is recognized:

```
systemd-cryptenroll --fido2-device=auto list
```

Note: You may need to install `libfido2`.

After confirm the Yubikey is recognized, use this command to enroll it:

```
systemd-cryptenroll --fido2-device=auto <disk-device>
```

It will show hint about you may need to press the Yubikey during the process. So **watch the Yubikey: when its LED flashes, press it to continue.**

## Setup crypttab

Put a line like this into `/etc/crypttab.initramfs`. It will be copied to initramfs by mkinitcpio as `/etc/crypttab` so that your root partition can be decrypted before it is mounted:

```
myvolume <disk-device> - fido2-device=auto
```

`<disk-device>` can be something like `/dev/sda1` or using UUID format `UUID=<disk-uuid>`.

If it's not root partition, you can put it in `/etc/crypttab` so it will be used after root partition is mounted.

## Setup mkinitcpio

`mkinitcpio` is a tool to generate initramfs. `/etc/crypttab.initramfs` only works with it. So if your distro comes with other tools like `dracut`, you may need to uninstall it and install `mkinitcpio` instead.

Once making sure `mkinitcpio` is installed, we need to configure the hooks to make it read `crypttab` to decrypt the disks. We also need to make sure we are using systemd init instead of busybox init.

Open `/etc/mkinitcpio.conf`, and find the line with `HOOKS=(...)`. Refer to [this wiki page](https://wiki.archlinux.org/title/Mkinitcpio#Common_hooks) about the common hooks and replace busybox hooks with systemd ones. For example, in my setup, I replaced `udev` with `systemd` and `keymap` with `sd-vconsole`. Then add `sd-encrypt` to the hooks. The order matters: usually it comes after `sd-vconsole`.

Then use `mkinitcpio -P` to regenerate initramfs images.

## Test and Finish!

Okay, now we have already setup everything. We can boot the system and test. Make sure Yubikey is plugged in before the boot. And watch for its LED light to flash and press it when it does! This little detail spent me lots of time to figure it out.

You can also use password to decrypt the disk **without Yubikey plugged in**. Wait it for 30 seconds and it will prompt you to input the password. The time can be configured in `/etc/crypttab` (or `/etc/crypttab.initramfs`) by setting up [token-timeout=](https://man.archlinux.org/man/crypttab.5).
