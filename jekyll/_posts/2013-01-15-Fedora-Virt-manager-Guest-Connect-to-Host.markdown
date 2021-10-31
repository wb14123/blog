---
layout: post
title: Fedora Virt-manager Guest Connect to Host
tags: [kvm, fedora, virt-manager, newwork]
index: ['/Computer Science/Virtualization']
---

These days I'm confused with the virt-manager's network. While I use the Fedora guest to connect to 192.168.122.1, it always connect to itself. I searched on the internet but there are little information. I mean, though the information, it should work.

I finally solved it today. It turns out the fedora guest also start `libvirtd`. Which will create another virt network and use itself as the host. If you run `ifconfig` on the guest, you will see it has a device `vnet0` with IP 192.168.122.1.

So just disable `libvirtd` on the guest, then restart `libvirtd` and virt-manager on the host. After all, you could connect to the host through 192.168.122.1 with the default configuration.

