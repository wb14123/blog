---
layout: post
title: Docker Is the One Scaffolding to Rule Them All
tags: [Docker]
index: ['/Computer Science/Virtualization']
---

> One Ring to rule them all, One Ring to find them,
>
> One Ring to bring them all and in the darkness bind them
>
> -- *The Load of the Rings*

Docker is popular these days. I don't like it at first.

Docker could build an OS template with the environment already setup. Include the softwares we need to use, and the deploy tools.

Let's look at the software dependency first. In my mind, a good written software could be installed by a package manager easily, and could be configured with files easily. All we need to do is backup the name, version and configure file of the software, then use a package manager to install them, then copy the configuration files.

Second, the deploy of projects. If the project is well written, it should include a script that is easy to use, easy to deploy the whole project.

I don't like scaffolding tools either at first. Such as the Maven Archetype plugin, Typesafe Active, and so on. They generate a project with many code, which I don't really understand. That doesn't feel right.

But things changed while I write more and more projects. I found I need to do many things when I start a project. There are always some code, such as a main function, that could not be put into a dependency library, though the code looks similar. So I copy some files and code from another project I wrote before. At this time, I realize I'm repeating myself. This time I realize I need a scaffolding tool to build a template that I could use in another project.

So I started write my own framework, my own template. Then I found, there are things that could not be done with a single scaffolding tool like Maven, Yeoman and so on. For example, I build a template project that you can just focus on build the business logic of a web service, then with a single command, you can get a WAR package that could be deployed to any servlet container.

But how to deploy them to the servlet container? You don't know what the container is and where it is installed. If we know, we could use a maven plugin to deploy it. But things are not always that simple in the real world. In different OS, container package is installed in different places. So we need to find where it is installed, then config it in the `pom.xml` of Maven.

What if we could specify the OS and the container? Project A let's call our project. OS A we need for project A. But what if we need to run project B on OS B? Oh, that is so messy.

So, why not just put all the things we need in the template, even include the OS. After that, you just write logic code, and just one click (or run one command), you can deploy it to the server. Whatever you need you can put them in the OS, it includes all the things. (Maybe a cluster includes every thing, Docker could do that, too.) So Docker is the load of the scaffolding tools. It rules everything you need that doesn't matter with the business logic.
