---
layout: post
title: "Build An Instant Messaging System without Censorship: Choose the Right Technology"
tags: [technology, Matrix, instant messaging, censorship]
---

*This article is the first one of a series of articles that talk about how to build an instant messaging system without censorship.*


Instant messaging (IM) software maybe the most commonly used type of software. Privacy is especially important for instant messaging apps. No one wants to be eavesdropped when talking with friends and family. However, most of the instant messaging applications don't have end to end encryption, which means the service provider can see all the messages. This information is much more sensitive than financial information, yet we have regulations for banks but have basically zero regulation for instant messaging providers. Sometimes it's even worse while having regulations: Chinese government can require the provider to hand over the server data [by law](http://www.cac.gov.cn/2016-11/07/c_1119867116_2.htm) (Ironically, the offical website of Cyberspace Administration of China doesn't even have https). That's why TikTok is such a hot topic recently. And while Tencent saying they never look at the messages in WeChat, I never trust them. There are even [news](https://news.qq.com/a/20151012/010241.htm#p=4) that showing QQ (Another IM software provided by Tencent that is very similar with WeChat) messages helped the police to find criminals. Even if the providers don't look at the data as they declared, as long as they store the data, it becomes permanent record. You don't know who will use that data on what purpose in the future.

There are a few IM software that have end to end encryption built in. However, each of them has some shortcomings which prevent them to be truly security. For example, [Signal](https://www.signal.org/) is considered as the most security one, but it needs a phone number and use it as verification sometimes. As we all know, text message is very easy to be hacked. While the hacker cannot view the message history in Signal, it may makes the account disappear. (The technology details behind this is complex, I may write another article to explain this in the future). WhatsApp is another popular one. But it's not open sourced so it's hard to tell if it's doing something secretly or really implementing the end to end algorithm correctly. Most important of all, end to end encryption doesn't mean decentralize. They all have centralized servers. And unfortunately, the servers are all blocked by China.

There are also some P2P IM software. I tried a lot of them. Most of them don't have end to end encryption. And they are all too slow to have a good user experience.

So the ideal solution would be deploying an instance messaging system by myself. Signal is open sourced. The server code is clean and easy to deploy. But as I said before, I don't like the use of phone number. And because it's not designed for decentralized deployment, the iOS and Android client needs to be modified to bind push keys from Apple and Google in order to have notifications. Downloading a customized version of client is not very user friendly.

Then I found [Matrix](https://matrix.org/), an open standard for instant messaging. It's decentralized by design. People can have different service providers like Email. People can speak with other persons on other server. The servers talk with each other to deliver messages. The username of Matrix contains the server name, so the servers can tell where to deliver messages. The diagram below shows how user1 on `server1.com` talks with user2 on `server2.com`.

```

 User: @user1:server1.com
          |    ^
          |    |
          V    |
 Server: server1.com
          |    ^
          |    |
          V    |
 Server: server2.com
          |    ^
          |    |
          V    |
 User: @user2:server2.com

```

While it has a lot of [clients](https://matrix.org/clients-matrix/), the most popular one is [Element](https://element.io/)(used to be named Riot). The most popular server is [Synapse](https://github.com/matrix-org/synapse). The authentication server is different from the messaging server. [Sydent](https://github.com/matrix-org/sydent) is a one of the implementations. Synapse and Sydent are both written in Python. While it takes some efforts to deploy it, they are both well documented and shouldn't be hard for someone with server admin experience.

## A Brief Overview of Internet Censorship Strategies in China

There are two parts of Internet censorship in China: for the network in the country and outside the country. The difference is because of the ability of control: they can control whatever is in the country, but cannot control the ones outside it.

### Inside China

Let's first talk about the censorship strategies in China. It's mainly implemented by mapping the instances on Internet to real persons or organizations.

For individuals, which is mainly the consumers of Internet services, it's done by bind the person's ID to all the accounts on Internet. Firs of all, you need to provide ID when you buy Internet service from ISP. Secondly, you need to provide a Chinese phone number for all the online accounts in China. You also need to provide ID when you buy a phone number. So the government can map any online account to a real person.

For the service providers, it's done by licensing. For anyone who wants to provide Internet service, they must apply to the government, providing the IP addresses, domain names, ID, personal photo and the business purpose. The government will store all the information and may request the provider to hand over information as needed.

### Outside China

The deployment would be much easier if there is no [Great Fire Wall](https://en.wikipedia.org/wiki/Great_Firewall#Blocking_methods) (GFW). It's built by Chinese government that filters and blocks the network traffics between China and other countries. As a victim of it for many years, I know it just too well. It has mainly these technologies to block traffics:

1. Change DNS record. DNS server is used to resolve domain name to IP address. ISP usually provide DNS servers and computers will use that by default so it would be faster. The strategy here is to change the DNS records that you want to block. However, it's the easiest to fix: just use another DNS server.
