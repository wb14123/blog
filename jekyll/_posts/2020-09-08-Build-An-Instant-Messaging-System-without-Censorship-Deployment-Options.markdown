---
layout: post
title: "Deploy Matrix for Users in China"
tags: [technology, Matrix, instant messaging]
---

*This article belongs of a series of articles that talk about how to build an instant messaging system without censorship:*

1. *[Matrix: A Self Hosted Instant Messaging Solution with End to End Encryption](/2020-08-23-Build-An-Instant_Messaging-System-without-Censorship-Choose-the-right-technology.html)*
2. *[Overview of China's Internet censorship strategy](/2020-08-29-Build-An-Instant_Messaging-System-without-Censorship-Choose-An-Overview-of-Chinese-Internet-Censorship-Strategy.html)*
3. *[Deploy Matrix for Users in China](/2020-09-08-Build-An-Instant-Messaging-System-without-Censorship-Deployment-Options.html)*

In the previous articles, we chose Matrix as our IM service solution. We also discussed how the Internet is censored in China. In this article, we will discuss multiple ways to deploy Matrix service. The goals are providing the best user experience while avoiding the censorship. Specifically, we want to meet these requirements:

1. Avoid the government censorship.
2. Make the latency between users and server as low as possible.
3. Make it easy for end users to setup.

We will explore the deployment options. And compare them at the end.

## 1. Deploy A Single IM Server Outside China

The easiest solution would be deploying a single Matrix server. There are a lot of requirements to deploy a server in China, so it would be better to deploy a server outside China:


```
   Users in China
           ^
           |
           V
        Server
           ^
           |
           V
 Users outside China
```

But as we discussed in the previous article, the latency for users in China will be high. Sometimes, GFW may completely block the server so that the users cannot connect at all.

Matrix is a distributed protocol, so we can deploy multiple servers to give the best experience for all users. From here, we will explore the options with multiple servers: a server outside China and at least one server in China.

## 2. Deploy A Server in China with License

The most regular way to deploy a service in China is to get all the required licenses. Then the users in China can connect to this server:

```
   Users in China
           ^
           |
           V
   Server in China  <----- Public DNS Record
           ^
           |
           V
 Server outside China
           ^
           |
           V
 Users outside China

```

The shortcomings are also obvious:

1. It needs some work to get a license. Especially it would be hard for an individual to get a license for IM service. It's possible to hide the business purpose while applying, but there are risks.
2. Once the service is on government's record, government can track it and require information from the server. Even the messages are end to end encrypted, some information on the server can still be sensitive.

## 3. Deploy A Server in China without DNS Resolving

The main reason we need a license to deploy a server in China is because the IDC and cloud providers may block the server if a domain without license is resolving to it. So one way to avoid the blocking is, don't bind a domain name to the server. But we cannot use IP address directly either: we need HTTPS to encrypt the traffic. And the communications between servers also depends on domain names.

So we need to find a way to resolve domain name without public DNS server. Though it's hard, it's not impossible. For example, you can change the DNS record on the computer by modifying the hosts file. Or you can create a DNS server on the home router. For the mobile phones, it's much harder. But I believe there are apps can do that.

So this solution is don't register the domain name in public DNS servers. Instead, add the records on end user devices:

```
   Users in China
   (Add DNS record)
           ^
           |
           V
   Server in China
           ^
           |
           V
 Server outside China
   (Add DNS record)
           ^
           |
           V
 Users outside China

```

## 4. Deploy Another Server in China with Personal Internet

As we mentioned in the last article, while cloud providers and IDC block servers, ISP doesn't block high number ports. So if we can deploy another server at home and point the domain name to this server, the end user doesn't need to setup custom DNS records. We will still keep the other server in cloud/IDC for the stable connection between servers.

For the two servers in China, only one of them needs to host the IM service. The other one can just be a proxy.


```
        Users in China
              ^
              |
              V
      Proxy server in China    <----- Public DNS Record
        (Home Internet)
              ^
              |
              V
      IM Server in China
         (Cloud/IDC)
              ^
              |
              V
     Server outside China
(Add DNS record that resolves domain
  name to server on Cloud/IDC)
              ^
              |
              V
    Users outside China

```

There are also disadvantages for this solution: other than we need another server, the home Internet may not be able to provide the best speed for users in China.

## Conclusion

In the table below, we can compare them by the censorship circumvention, service speed and end user setup easiness:

| Deployment Option |  Censorship Circumvention |  Service Speed | End User Setup Easiness
| :---------------- | :------------------------ | :------------- | :-----------------
| 1                 |  3 stars                  |  1 star        |  3 stars
| 2                 |  1 star                   |  3 stars       |  3 stars
| 3                 |  2 stars                  |  3 stars       |  1 star
| 4                 |  2 stars                  |  2 stars       |  3 stars

I believe the world will be a better place if everyone can communicate with each other freely. But it's never easy. Even though we can deploy the services and make it accessible from China, the most popular Matrix client Element (Riot) is banned in China. There is a long way to go. I hope I can help even a little bit by sharing some technical information here.
