---
layout: post
title: How About Translate IMAP And SMTP Into HTTP API?
tags: [programming, email, thought]
index: ['/Computer Science/Software Engineering']
---

I use Gmail as my personal Email. And it works well. I always uses the web UI for it. After Google Inbox is out, it feels even better.

But my company uses another Email service, which their web UI is not so great to use. So I need an Email client to notify me while there are new Emails, and make some filters to archive different kinds of Emails. Since I'm using Mac OS, so I tried the Mail app that comes with the system, MailBox and Thunder Bird. Then I found, they all worked terrible. I tried to use them with my Gmail account. There are lots of Emails in my Gmail accout since I subscribe many mail list. It takes very long time to sync the mails from the server. And when I click one mail, it loads very slow, too.

I've used Thunder Bird before some years ago while I'm using Linux. I don't remember it is not so bad. Then I realize, time is changing and the technology is developing. The technologies today makes things like website works really smooth. So why not Email technology take advantage over that? Why the clients still needs to sync many Emails from server even I don't need to see all of them?

So here comes an idea that sounds interesting. I don't have time to develop it right now so I write it here in case I forget it. The idea is, why not translate IMAP and SMTP protocol into HTTP RESTful API?

Here is the architecture: there is a server and a client. The server load all Emails from the Email server and store the messages in its own database. And it provide some APIs so that the client could just query this server or send Email through it instead of the old IMAP or SMTP protocol.

Here are some APIs that the server could provide:

* Get folders and its meta data (How many messages in it for example). You can specify the parent folder or just get all of the top folders.
* Get messages from one folder. With pagination.
* Get and modify contacts. With pagination.
* Get and modify filters.
* Search the Emails and contacts. (This could be done with Elastics for example).
* Send an Email.

They are just some APIs that come out of my mind in a few minutes, but it does make sense. With these APIs, the client could be written with web technologies which are more flexible, and the server could use more modern technologies such as Redis or RDBMS to improve performance.

