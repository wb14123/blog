---
layout: post
title: "Source Code of RSS Brain is Available"
tags: [RSS Brain, open source, source available, project, software engineer]
index: ['/Projects/RSS Brain']
---

*This article is also posted at [RSS Brain blog](https://news.rssbrain.com/news/2024/07/26/Source-Code-Released).*

When I first published [RSS Brain](https://www.rssbrain.com/), I promised the source code will be released (well, I actually said "open source", but more on that later). After I rewrote the whole Flutter frontend with Javascript, most code is put into a single source repo. I feel comfortable to release it. So here it is on [Github](https://github.com/wb14123/rss_brain_release).

There are two things you may notice from the source code:

* The commit history is mostly missing.
* The code license is not an open source license.

I'll talk about the most important one first: the code license.

## Code License

RSS Brain's source code is released under [SSPL](https://github.com/wb14123/rss_brain_release?tab=License-1-ov-file#readme), Server Side Public License. I don't want to use "open source" as a market point for RSS Brain so I must make this clear first: technically, RSS Brain is a [source available software](https://en.wikipedia.org/wiki/Source-available_software), not an open source one, since SSPL is not recognized as an open source license.

SSPL is mostly the same as [AGPL v3](https://www.gnu.org/licenses/agpl-3.0.en.html), but with a key difference: it requires the user to release the source code of the whole stack if the project is used commercially. If you want to run the code on your own server and use RSS Brain freely, it's all good. You can even share your server with family and friends. But as long as you start to charge money for that service, you need to release the source code of everything you use for the service, including things like OS, CI/CD, web server and so on. So it basically makes it impractical to use the source code commercially. I chose that feature on purpose.

## The Purpose

Before I explain why I chose this license, I must explain the reasons of making RSS Brain's source code available.

### Transparent Algorithm

In a past blog post [What Is Wrong about Recommendation System](https://www.binwang.me/2020-08-02-What-Is-Wrong-abount-Recommendation-System.html), I mentioned I don't want to manipulated by recommendation systems. Ant that's one of the main motivations for me to start write my own RSS reader. While there are still ranking and recommendation algorithms in RSS Brain, it's aimed to provider better information instead of making the user more addicted to the product. In order to approve that, the algorithm needs to be available so that the users can inspect it and decide whether it's the right one for them.

Be aware even the source code is available, it still needs some level of trust since the code running on my server [app.rssbrain.com](https://app.rssbrain.com) can theoretically be different from what is being released. But it's good enough for most people. However, if you want absolute control, you can always run it on your own server with the source code available.

### No Vendor Lock-in

Another important benefit is the user can expect the software to last. Even if I don't host the service anymore, there is always a way to continue using it since the code is available. Yes it's not commercial friendly, but if the software turned out to be really useful and attracted enthusiasts, I believe someone else will continue to maintain it for free. I think this property is critical for any product that needs to be used every day and becomes an important part of the digital life.

### No Free Commercial Usage

The next benefit is not for the user, but for myself. I want the **users** be able to self host the service for free, but I don't want other people take my source code for free and earn money from it. I think that's reasonable, especially considering I also provide paid hosted solution at [app.rssbrain.com](https://app.rssbrain.com).

### Considerations of Contributors

One big advantage of open source project is it can attract contributors to make the software better. And it can sometimes justify the free commercial usage because all the competitors are contributing to the software. But because this is a software I am and will use daily, I want to have 100% control of the roadmap of it. Not only the product aspect, but also technology aspect. It's just easier to write all the things by myself, at least for now. So I'm fine to chose a non open source license even with less potential contributors.


## Release Process

You may notice the source code has very few commit history. The release process will be only one commit for each release. The release cycle will be one release every few weeks, depends on how much process I make. The regular releases will mostly on the weekend. If there is a bug or a security risk, the release maybe more frequent.

The version number is in the format of `X.Y.Z`. Where Y will be increased for every feature release and Z will be increased for every bug fix. X will only be increased for breaking changes or really major update.

I'll make my hosted version at [app.rssbrain.com](https://app.rssbrain.com) the same as the source code. Which means at each release, I'll update the app first, and release the source code just after it. I'll add a section in the app's setting page to indicate the current version.

The reason I chose this release mode is the same reason as I released it under SSPL. I only want the source code be available to users, but I don't really care about whether there will be contributions from other people. So hide the commit history between releases just make my life easier since I don't need to care too much about keep my commit messages clean.

## Roadmap

With this source code released, everyone can inspect the algorithm to decide if this is the right product for them. However, for self-host, even if you can do it right now, it requires some undocumented configuration. So I'll do the following things to make it easier:

* Add documents for self-host.
* Add documents for admin operations like create admin users.
* Disable some components by default. To name a few:
  + There is an machine learning server mentioned in [this blog](https://www.binwang.me/2023-11-14-Update-On-RSS-Brain-to-Find-Related-Articles-with-Machine-Learning.html). I will likely disable it by default since I'm thinking about redo this part in the short future.
  + Payment is not needed for self-hosted instances so I'll disable it by default.
  + There is an image proxy that I'll likely to disable as well, just to make it easier to deploy.
