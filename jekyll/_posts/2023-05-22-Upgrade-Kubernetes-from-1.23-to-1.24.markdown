---
layout: post
title: Upgrade Kubernetes from 1.23 to 1.24
tags: [Kubernetes, container, Docker, Linux]
index: ['/Computer Science/Distributed System Infrastructure']
---

In the [last blog post](/2023-03-13-Infrastructure-Setup-for-High-Availability.html), I introduced using Kubernetes to setup high available infrastructure. I had that setup a long time ago. I did the long overdue upgrade for Kubernetes from 1.23 to 1.24 recently. Since GlusterFS is [deprecated](https://github.com/kubernetes/kubernetes/pull/111485)(though not removed) in 1.25, I have no plans to continue the upgrade without exploring alternative storage options.

There is a big change from 1.23 to 1.24 as well, namely, [Docker Engine support has been removed](https://kubernetes.io/blog/2022/03/31/ready-for-dockershim-removal/). I migrated the container engine to containerd. But the process is not without pain. I need to search different sources to fix the issues. So I list my upgrade steps so that if anyone has the same issue, this may help.

My Kubernetes cluster is set up locally with `kubeadm`. There is an [official upgrade guide](https://v1-24.docs.kubernetes.io/docs/tasks/administer-cluster/kubeadm/kubeadm-upgrade/) for kubeadm to upgrade from 1.23 to 1.24, but it doesn't mention any steps to remove Docker and setup containerd. So here are the steps I took:

1. Add `--container-runtime-endpoint` option to kubelet. The way I did it is adding `KUBELET_ARGS="--container-runtime-endpoint=/run/containerd/containerd.sock"` to `/etc/kubernetes/kublet.env`. Without this, Kubelet will fail to start.
2. Remove `--network-plugin=cni` from  `/var/lib/kubelet/kubeadm-flags.env`.
3. Add the following configuration in `/etc/crictl.yaml`, otherwise kubeadm will not be able to pull needed images:
```
runtime-endpoint: unix:///run/containerd/containerd.sock
image-endpoint: unix:///run/containerd/containerd.sock
timeout: 10
debug: false
```
4. Configure `SystemdCgroup` permission for containerd. Otherwise kube-apiserver will always be restarted because of "sandbox environment changes" (see more in [Github issue](https://github.com/kubernetes/kubernetes/issues/110177)):
```
sudo mkdir -p /etc/containerd/
containerd config default | sudo tee /etc/containerd/config.toml
sudo sed -i 's/SystemdCgroup \= false/SystemdCgroup \= true/g' /etc/containerd/config.toml
sudo systemctl restart containerd
```
5. Follow the [official upgrade guide](https://v1-24.docs.kubernetes.io/docs/tasks/administer-cluster/kubeadm/kubeadm-upgrade/).
6. After the upgrade, remember to restart Docker so that the old containers started by Docker will be stopped.
