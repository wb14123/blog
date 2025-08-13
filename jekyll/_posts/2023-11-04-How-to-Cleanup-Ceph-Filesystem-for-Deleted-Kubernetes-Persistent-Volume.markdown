---
layout: post
title: How to Cleanup Ceph Filesystem for Deleted Kubernetes Persistent Volume
tags: [Kubernetes, Ceph, Distributed file system]
index: ['/Computer Science/Distributed System Infrastructure']
---

[Ceph](https://docs.ceph.com) is a distributed file system. [Rook](https://rook.io/) is a project to deploy it with Kubernetes. I recently replaced GlusterFS in my Kubernetes cluster with Ceph. I will write a blog (or a series of blogs) for the migration. But in this article, I will just talk about a problem I encountered, just in case I forget it.

Once Rook is deployed in Kubernetes, you can create a [Ceph Filesystem](https://rook.io/docs/rook/v1.11/Storage-Configuration/Shared-Filesystem-CephFS/filesystem-storage/) and use it for [persistent volume (PV)](https://kubernetes.io/docs/concepts/storage/persistent-volumes). Each PV's data will be stored in a folder in the filesystem. If the PV's reclaim policy is set to [retain](https://kubernetes.io/docs/concepts/storage/persistent-volumes/#retain), the data will not be deleted after the persistent volume is manually deleted. It's safer in this way. But what could you do if you want to clean up the data? Normally you should change the PV's reclaim policy before you delete the PV, then Rook's operator will automatically reclaim the storage in Ceph. But what if you forgot or didn't know that (like me), and want to clean up the data after?

First, we need to find the folder/subvolume names in Ceph that store each PV's data. We can get that by using `kubectl describe pv <pv-name>` and look for the field `subvolumeName`. But since the PV is deleted, we need to find the mappings for existing PVs and compare that with the folders/subvolumes in Ceph. This is the command to show all of the existing ones:

```
kubectl get pv -o yaml | grep subvolumeName  | sort
```

Then we need to find all the existing folders/subvolumes in Ceph's filesystem: Start a Ceph toolbox pod based on the [doc](https://rook.github.io/docs/rook/v1.11/Troubleshooting/ceph-toolbox/?h=toolbox). Then go into the pod and find the filesystem's name first:

```
ceph fs ls
```

After getting the filesystem's name, get all the subvolumes from it:

```
ceph fs subvolume ls <fs-name> csi | grep 'name' | sort
```

Compare this list with the list above, you should be able to find a subvolume that exists in Ceph but not shown in Kubernetes' PV mapping. Use this command to check its info:

```
ceph fs subvolume info <fs-name> <subvolume-name> csi
```

If you are sure this is the folder you want to delete, use this command to delete it:

```
ceph fs subvolume rm <fs-name> <subvolume-name> csi
```


