---
layout: post
title: Compile And Install Kernel
categories: notes
tags: [linux, kernel]
index: ['/Computer Science/Operating System/Linux']
---

There are already many documents to teach one how to compile and install the kernel. Here are just some key notes. Use `make help` under the source directory for details.

## 0. Kernel configure

It is helpful to have a kernel git repo. Use `git tag` to see the kernel versions and use `git checkout <tag>` to checkout the specified version, such as:

    git checkout v3.7-rc6

It is recommended to use the distribution's kernel configuration file as a basic configuration when you compile the kernel for the first time. Copy it from `/boot` to `<build_dir>/.config`. Use this command to modify it:

    make O=<build_dir> menuconfig

It will save the new configuration and rename the old configuration with `.config.old`.

## 1. Kernel image

Kernel image is the most important part. Use this command to compile the kernel in `<build_dir>`:

    make O=<build_dir> -j4

After that, copy `vmlinux` and `System.map` to `/boot`. Then configure the boot loader. Or use the command `installkernel` if you have it.

## 2. Kernel modules

Some code is compiled to kernel modules. It needs to be installed to the system. Use this command to install kernel modules and firmwares: 

    make O=<build_dir> modules_install

It installs kernel modules under `/usr/lib/modules/<kernel_version>`. You can also specify the output directory using `INSTALL_MOD_PATH`:

    make O=<build_dir> INSTALL_MOD_PATH=<module_dir> modules_install

The `INSTALL_MOD_PATH` is `/` by default.

## 3. Linux headers

Linux headers are header files invoked by user space programs. Use this command to output them:

    make O=<build_dir> INSTALL_HDR_PATH=<headers_dir> headers_install

It installs headers to `<headers_dir>`. You should copy them into `/usr/include`.

## Another way

After all, you may want to make a kernel package to install on other machines. Such as:

    make O=<build_dir> -j4 binrpm-pkg
