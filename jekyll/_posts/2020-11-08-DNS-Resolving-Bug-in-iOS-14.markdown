---
layout: post
title: DNS Resolving Bug in iOS 14
tags: [DNS, iOS, technology, self hosted]
---

## The Bug Description

iOS 14 has a bug for DNS resolving under this circumstances:

* Manually specify a custom DNS server for a WiFi network.
* For a domain, this custom DNS server has different DNS record than the default public DNS server.
* The DNS record type of this domain is CNAME on public DNS server.

Under this setup, after connecting to WiFi with custom DNS, iOS 14 should get the IP for this domain according the record on custom DNS server. However, it still gets the IP that's on public DNS server.

Here is an example. The table below shows the DNS records on public DNS servers:

| Domain   | DNS Record Type | Value      |
| :--------|:----------------|:-----------|
| domain-1 | CNAME           | domain-2   |
| domain-2 | A               | 1.1.1.1    |

So `domain-1` will be resolved to `1.1.1.1` by using the default public DNS server.

Then we have a custom DNS server, which modifies record for `domain-1`. For other records, it uses the upstream ones:

| Domain   | DNS Record Type | Value      |
| :--------|:----------------|:-----------|
| domain-1 | A               | 2.2.2.2    |
| domain-2 | A               | 1.1.1.1    |

So if you use the custom DNS server, `domain-1` should be resolved to `2.2.2.2`.

However in iOS 14, even if you manually specify the custom DNS server for the WiFi network, `domain-1` is still resolved to `1.1.1.1`.

I guess this is because of DNS cache problem. I tried to clean up the DNS cache by changing my device to airplane mode, reboot the device, or stay in the WiFi with custom DNS for days. Despite all the attempts, the problem still exists.

I filled a bug report to Apple but didn't get any response after almost one month. So I think I can share it here so it may help someone else with the same problem.

## How Do I know It's not the Problem of My DNS Setup

The setup above works on every other devices I have (Linux devices, MacOS devices). It also works on the same iOS device before I upgraded it to iOS 14. I also installed the app [Network Analyzer Pro](https://apps.apple.com/us/app/network-analyzer-pro/id557405467) to debug the setup. In Network Analyzer Pro, if I use the DNS resolving tool with the custom DNS server, it can resolve the right IP address. But if I ping the domain directly, it resolved to the wrong IP. So there is something wrong at the system level of iOS 14.

## Workaround

This bug is very frustrating. I spent lots of time to identify what's the issue: for the application, to the permission setup (iOS 14 has a new local network permission), to the system. And finally found it's iOS system DNS resolving problem and found a workaround.

The workaround is to add a DNS record for the domain behind CNAME (in this case `domain-2`) into the custom DNS server:

| Domain   | DNS Record Type | Value      |
| :--------|:----------------|:-----------|
| domain-1 | A               | 2.2.2.2    |
| domain-2 | A               | 2.2.2.2    |

However, if you have multiple domains point to `domain-2` and don't want to change IP addresses for those domains, this workaround may not be able to support that use case.

## A Little More Story

It may looks weird that I have a DNS setup like this. I host some services on my desktop machine. Because of my home router provided by ISP disabled NAT loopback (which means the router denies all the traffic that comes from itself), I must use the IP address in local network to access the services if my devices are in the same network. So I setup a custom DNS server that resolves the service domains to my desktop's internal IP address, and use this DNS server when I'm using home WiFi.

I depend on this workflow heavily. Everything worked smoothly until I upgraded to iOS 14. For more than 1 month, under the home WiFi connection (which is most of the time because of CoVID-19), I cannot sync my calendar or notes, or fetch an E-Book from my collections to iPad, or upload my photos automatically, or chat with my friends on phone, and so on. It's very frustrating and is still not fixed after two version upgrades of iOS 14.

After my previous Android phone broken, I decided to buy an old generation iPhone. One big reason is it's really cheap with the carrier contract. I'm trying to reduce my time on phones so an old generate is more than enough. In many ways it's much better than an Android phone: for an Android phone, I need so many tweaks to make sure it respects my privacy and apps not running at background all the time. And after that, many apps are not usable or don't have proper notifications. On contrast, Apple controls the ecosystem strongly to make sure the developers don't abuse the system. But on the other hand, Apple also controls the users strongly. It's hard to downgrade the system. It's hard to debug the system. It's even hard to submit a bug report: you need to enroll Beta profile and use an app to submit it. (I'm not sure if there are other ways but it's the suggested way on Apple's website). With an Apple mobile device, I feel more like renting it instead of owning it. I really hope one day there is a device that's both open to users and have a strong permission management to limit the app behaviors (including the apps owned by device provider). It must be hard based on the user base, but doesn't hurt to have some hopes.
