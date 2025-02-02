---
layout: post
title: Replace A Dead Node in My High Available Cluster
tags: [Kubernetes, infrastructure, high availability]
index: ['/Computer Science/Distributed System Infrastructure']
---

In my previous blogs [[1]](/2023-03-13-Infrastructure-Setup-for-High-Availability.html)[[2]](/2023-11-28-Introduce-K3s-CephFS-and-MetalLB-to-My-High-Avaliable-Cluster.html), I've introduced my high available cluster setup. It works really well: when 1 of the 3 nodes is down, the service either continue to be online, or can be recovered rather quickly (in the case I set service replica to 1 to not wasting the resource). However, in the beginning of this year, one node is down not because of regular updates or temporary shutdown for maintenance, but because its system disk is dead. While it's annoying to replace the disk and bring it back, it's actually a good opportunity to verify a dead node can be replaced in my setup. So I will note the steps down in this article. This will be a short one but it shows how easy it is.

## What Has Lost?

The dead disk is the system disk. It has the OS, but also has the data for CockroachDB and ElasticSearch. However, since the data for CockroachDB and ElasticSearch is replicated across the cluster, it can be recovered from other machines.

The machine also has a separate disk for CephFS but that disk is not lost. The data in CephFS is also replicated so should be able to recover from other machines as well even if it's dead. But it may need additional setup, like changing the disk uuid in Rook's Kubernetes manifests.

## Why Not Recover From Backup?

First of all, I don't backup that often because I don't feel the need considering the data is replicated. Another reason is, I setup this machine based on the usage of [offsite online backup](/2021-09-19-Personal-ZFS-Offsite-Online-Backup-Solution.html). Then I repurposed it to use in this HA cluster. I want to change the secure boot setup because the threat model is different so it doesn't need such complex boot setup, which is not supported very well by mainstream Linux without TPM 2.0.

Since the data can all be recovered from other machines automatically, it would be easier to just install a fresh OS and some basic infrastructure so that all the service deployments and data can be auto recovered. This also simulates a dead node situation, so that I have more confidence for recovering from such failures in the future.

## How to Recover?

Okay, here we are for the actual recovery steps. It's very simple:

First, install the OS. Configure basic things like network IP address, ssh, etc. Install things like `prometheus-node-exporter` if you are using it on other machines.

Next step is to let the node to join our Kubernetes cluster. Before that, we can remove the old dead node in the Kubernetes cluster by using the command `kubectl delete node ...`.

Then install k3s: Copy the config file under `/etc/rancher/k3s/config.yaml` from another machine and adjust the node IP and network interface config. Make sure the config has something like `server: https://...:6443` so it will join the existing cluster instead of creating a new cluster. Check the k3s versions on other machines by using `kubectl get nodes -o wide`. Then install it with `curl -sfL https://get.k3s.io | INSTALL_K3S_VERSION=v1.31.1+k3s1 sh -` assuming `v1.31.1+k3s1` is the version.

After k3s is installed, the k3s service should be enabled by default and the node should join our cluster automatically. If the hostname and IP address are the same as the dead machine, the Kubernetes cluster should automatically reschedule the services on to this machine. If there are some failed containers, check the log to see if it's because the local directory for the storage is missing. In my case, I need to create the local directory for CockroachDB and ElasticSearch, and set the owner to 1000 for ElasticSearch.

At last, we need to make sure CephFS is working. Make sure `ceph` and `rbd` can be loaded with `modprobe`. If so, add them to `/etc/modules-load.d` to load on boot:

```
cat /etc/modules-load.d/ceph.conf
ceph
cat /etc/modules-load.d/rbd.conf
rbd
```
