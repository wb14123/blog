---
layout: post
title: Define Infrastructure as Code
tags: [cloud, docker, aws, cdk, technology, programming]
---

I'm using a lot of [CDK](https://aws.amazon.com/cdk/) at work recently. So in this article, I want to talk about defining infrastructure as code.

## Why Define Infrastructure as Code?

Why do we want to define infrastructure as code? The most important reason is we can make the progress automatic. After all, that's the purpose of programming. Normally, there are many manual steps to setup the infrastructure, like buying the machine, installing OS, setting up the network and so on. Luckily, with the cloud providers to provide the hardware, and with technologies like Docker and Kubernetes, it's possible to make more and more steps automatic.

But it's not always easier to automatically do things than manually do it. Another useful feature of code is it can be the source of truth and make the process reproducible. It's like the document, but more precise and complete. If you have the code, you can build all the systems from scratch without rely on other people's undocumented knowledge. It can make sure what you have is a clean system, without random things that people did and forgot. And since it's reproducible, you can improve the process and know whether it's better or worse.

The third advantage is you can use version control to manage the infrastructures. You can rollback to a good state. You can also see what has been changed from beginning, which is like an even better document.

## Define Operating System

For a software, the most related infrastructure is the operating system and the dependencies it needs on the operating system. Traditionally, while using physical machine, the operating system can be automatically installed by PXE through network booting. But it's hard to setup and test: you need a physical machine to do that.

After virtualization and cloud is popular, it's easier to build operating system images and test them. Since it's virtual machine, you can test it at your own development desktop and things will be the same while it's running on production. There are some tools like [Vagrant](https://www.vagrantup.com/) to do it. But it also has it's own disadvantage: it's hard to track what you really did in the image. It's true there are some tools to build the image with code, but I don't find it to be a normal practise.

Then there comes Docker and it makes defining operation system as code much easier. There are multiple reasons: it's command line by default so it's easier to write script. And it's normal practise to build image with Dockerfile instead of manually doing things in the image and save it. It's even easier to test it since it's more lightweight. It also has version number in it's image format, which makes people have version control in mind.

## Define Hardware Resource and Cluster Structure

On some level, Docker can define the hardware resource, like how much CPU and memory to use, file volumes and local network. But it can only define these at local machine level. When you want to run the service on a cluster and talk to other services, you need more powerful tool. There are many container orchestration tools but after these years it seems Kubernetes has become the standard. With Kubernetes, the things beyond operating system, like network structure, service structure and so on, can also be defined within yaml files.

The feature is not only available in container world. Even before containers are popular, lots of cloud providers have the ability to use code define the resources. It can not only define the hardware resource and network structure, but also for basically every service the cloud provides, like hosted database, logging management and so on.

## Use Code Instead of Configuration Files

For tools like Kubernetes which can use yaml configuration files to define infrastructures, it's trivial to use code generate the configuration files. Why we want to use code instead of using configuration files directly? Because sometimes there are something can be reused, or some logic depends on different situations.

Of course most tools include Kubernetes also provide API that you can call with code. But I think the better way is to generate configuration file about what you want, and let the tool to do all the actual work. It's like declarative programming and functional programming. Infrastructure creation is very complex and error-phone: the things can go wrong include how to safely change from one state to another one, failure handling, resource cleanup, rollback, rolling upgrade and so on. It's better to just describe what's the desired state and let the tools to do the actual work. It also makes it easier to optimize the workflow to make it faster.

CDK is such a tool that supports lots of programming languages. After run the code it can generate AWS Cloud Formation and run it. Unfortunately it's from AWS which means you are basically locked into AWS if you choose to use it. Though there is a project [cdk8s](https://cdk8s.io/) tries to support Kubernetes for CDK, I'm not sure if it gets the first level support. I really hope there would be an open source project like CDK that supports major cloud solutions.
