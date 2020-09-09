---
layout: post
title: "An Overview of China's Internet Censorship Strategy"
tags: [technology, GFW, China, Internet]
---

*Updated at sep 6, 2020: change the title and add other articles in this series.*

*This article belongs of a series of articles that talk about how to build an instant messaging system without censorship:*

1. *[Matrix: A Self Hosted Instant Messaging Solution with End to End Encryption](/2020-08-23-Build-An-Instant_Messaging-System-without-Censorship-Choose-the-right-technology.html)*
2. *[Overview of China's Internet censorship strategy](/2020-08-29-Build-An-Instant_Messaging-System-without-Censorship-Choose-An-Overview-of-Chinese-Internet-Censorship-Strategy.html)*
3. *[Deploy Matrix for Users in China](/2020-09-08-Build-An-Instant-Messaging-System-without-Censorship-Deployment-Options.html)*


There are two parts of Internet censorship in China:

1. Intra country network traffic: the sender and the receiver are all located in China.
2. Inter country network traffic: sender or receiver is in China, and another one is outside China.

The difference is because Chinese government has more control if all the devices within the network traffic are in China. It can make rules, and force ISP, IDC, cloud providers and service providers to follow. Otherwise, it can only uses some technical methods to block traffic.

## Intra Country Traffic

Let's first talk about the censorship strategy for the network traffic inside China. It's done by mapping the instances on Internet to real persons and organizations.

For the online service consumers, which are usually individuals, it's done by mapping the personal ID to Internet instances like IP addresses and service accounts. An ID is required when apply for Internet access from ISP and when apply for a phone number. When register an account for any online service, a phone number is needed. So it can map network information in these ways:

1. If you are using a home Internet, it can map from IP address to ID.
2. If you are using a mobile network, it can map from IP address to phone number to ID.
3. If you are using any online account, it can map from account to phone number to ID.

For the online service providers, it's done by licensing. For anyone who wants to provide Internet service, they must apply for a license from government. In order to get a license, you need to provide the hosts IP addresses, domain names, ID, personal photo and the business purpose. The government will store all the information and may request the provider to hand over information as needed.

In China, without a license, the network ports like 53 (DNS), 80 (HTTP) and 443 (HTTPS) are blocked by ISP for all devices. Cloud providers and IDC will also block hosts if they find any DNS without license is resolved to them. Home internet providers will not check DNS resolving, but it's impractical to host any serious business on home Internet: it would be too slow for people using other ISP.

## Inter Country Traffic

The censorship of inter country traffic is mainly done by [Great Fire Wall](https://en.wikipedia.org/wiki/Great_Firewall#Blocking_methods) (GFW). It's built by Chinese government that filters and blocks the network traffics between China and other countries. It uses a lot of university resources, and is widely known even there is no official acknowledgement of its existence. As a victim of it for many years, I know it just too well. It has mainly these technologies to block traffics:

First of all, DNS records pollution. DNS server is used to resolve domain name to IP address. ISP usually provide DNS server. The devices will use that by default to have the best speed. So if the government wants to block some service, it will change DNS records on DNS servers in China. Usually it's the first method to use. It's also the easiest to fix: just change to another DNS server. Usually a good DNS server outside China is blocked by IP (another blocking method we will talk below). Even it's illegal to host custom DNS server, you can still change the DNS records on home router or on the personal computer. However, it can be tricky for mobile phones.

Another straightforward blocking method is to block the IP addresses. It's not the most reliable method because the IP addresses can change. So normally it will block a range of IP addresses. But it's also dangerous because of the popularity of cloud services. The range of IP may belongs to the cloud provider instead of the service provider. So it may block more IP addresses than needed. It can accidentally block other important foreign services. So the government used to use this method carefully, but I feel it doesn't care to block innocent IPs recently. For example, a lot of CDN and small websites are not useable because of this. If the IP is blocked, the only way to access it is using a proxy.

After people start using proxy to access the blocked hosts, GFW develops new methods to block proxies. At first it's easy, because the proxy methods like VPN is very easy to identify. (Because VPN also has legit usages, the government also starts to require license for VPN). After people start developing proxy software specific to bypass GFW, GFW starts to detect patterns to identify and block these proxies. It's very hard to detect patterns with 100% accuracy, so it needs to choose either to miss some of the proxies or block some of the legit usages. And recently it tends to choose the later one more and more.

However, in the latest case, it has an exception. For the hosts provided by IDC or cloud services, it tends to have a looser restriction. For example, if I ssh to a personal computer in China from Canada, the connection will usually be disconnected after some time. But if I ssh to a server in China, it's usually very stable. It probably considers these hosts have more serious business purpose.


## Conclusion

We had an overview of the Internet censorship methods in China. I believe it will get back fired once the restrictions are more and more strict and ridiculous. In the next article, we will explore how to deploy an IM server in such environment and make it as convenience as possible for end users.
