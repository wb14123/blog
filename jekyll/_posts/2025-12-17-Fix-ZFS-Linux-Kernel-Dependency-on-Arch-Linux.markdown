---
layout: post
title: Fix ZFS Linux Kernel Dependency on Arch Linux
tags: [Linux, ZFS, dependency]
index: [/Computer Science/Operating System/Linux]
---

When updating an Arch Linux system, if you have some third-party repos added, the packages in them sometimes depend on an older package that's not available in the official repos anymore. In such cases, pacman cannot upgrade the system unless you exclude the impacted package.

ZFS is an example of this. Since Arch Linux doesn't ship ZFS packages in official repos, I added a third-party one [archzfs](https://github.com/archzfs/archzfs/wiki). However, the ZFS support doesn't always catch up with the newest kernel. When this happens, the upgrade will break.

There is another repo that's supposed to host the matching kernel version. It can be added by the following section in `/etc/pacman.conf`:

```
[zfs-linux]
Server = http://kernels.archzfs.com/$repo/
```

However, it also often lags behind. See [this Github issue](https://github.com/endreszabo/kernels.archzfs.com/issues/12) for the most recent example.

Luckily, Arch Linux has archives for old packages. There are command line tools like [downgrade](https://github.com/archlinux-downgrade/downgrade) to install the packages from archives instead of from the repo. So we can install the desired version of dependencies with `downgrade`.

When installing a specific version with downgrade, if it breaks other packages, it will refuse to continue installing. You can resolve it by installing multiple packages at once in the dependency chain, for example:

```bash
sudo downgrade linux linux-headers zfs-linux
```

It will ask you version for each package.

When you run `pacman -Syu` again, you will still get an error like this:

```
error: failed to prepare transaction (could not satisfy dependencies)
:: installing linux (6.18.1.arch1-2) breaks dependency 'linux=6.17.9.arch1-1' required by zfs-linux
```

But it's safe to ignore the kernel related packages now by using the `--ignore` flag:

```bash
sudo pacman -Syu --ignore linux --ignore linux-headers
```

**Important**: you may have noticed that we include the package `linux-headers` in the commands above, even though pacman doesn't complain if we don't do that. That's because in Arch Linux, `linux-headers` doesn't depend on a specific version of `linux`. However, if you have a version mismatch, it may break some dkms modules. So it's better to always keep them in sync.


