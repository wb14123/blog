---
layout: post
title: Move from Twitter to Mastodon
tags: [social network, Twitter, Mastodon, technology]
index: ['/Computer Science/Digital Life']
---

I'm very happy to announce that I moved to [Mastodon](https://mastodon.binwang.me/@wangbin) from Twitter.

## What's Mastodon?

[Mastodon](https://joinmastodon.org/) is a distributed version of Twitter. Anyone can host their own instance of Mastodon and still be able to follow and communicate with users on other instances. Think about Email: no company owns Email. Instead, there are lots of companies provide Email service. They are built on the same protocol, so the users on different providers can communicate with each other.

## Why Mastodon?

Mastodon is decentralized by design and open sourced. So I can host my own instance. This means I can actually own **my** data. This is very important: you don't know when the platform has a new policy that will delete your posts. That actually happened to me before: There is a Chinese version of Twitter called [Weibo](weibo.com/). Twitter is not available in China because GFW, so a lot of Chinese are using Weibo, which include me, a very early user of Weibo. I wrote a lot of things on Weibo: my ideas, my life and some poems. But after I shared an AI project, the whole account was deleted without notice. They don't even provide the reason or respond to my question. I only get some information from my friend who was working at Sina (the company behind Weibo), that it's because of some people are using the project to generate some political related stuffs. I was shocked and disappointed. I lost all my data on Weibo: the posts, the people I followed and the direct messages. Though Twitter is much better because it's not influenced by a dictator government, it also has it's own policy. I'm also afraid if my principle is conflicted with its policy. After all, what's the point of thinking if you cannot think freely.

Another advantage of an open source project is, the users are always the first. Because if it's not, other people (including myself!) will fork it, and users will select what meets their needs best. It happened many times: OpenOffice to LibreOffice, OwnCloud to NextCloud, MySQl to MariaDB, and so on. Even sometimes the original project is still the most popular one, people will still create other ones that are popular among specific groups. Such as Gnome 3 and Cinnamon. The reason behind this is it's much easier to create competitors. So while some people think open source is communism, I think it's more like capitalism in the sense of free market. Back to the case of Twitter, Twitter really pissed me off because of it sends recommendation to my notification without the option to turn it off. And because of it changes the timeline order to hot Tweets randomly. Twitter is doing this only on its own interest, and people cannot leave because of it's monopoly. But we have another choice now, which almost impossible to be monopoly. So why not move to the new one?

## How do I use Mastodon?

As I said before, I host my own instance of Mastodon. It's running on my own computer and my home's network bandwidth is more than enough for this usage. So it basically has no cost. The instance will mainly used by myself. Other people can still register on this instance but it needs my approval to active the account.

The use case will be exactly the same as Twitter. It will definitely has less audiences than Twitter, but I don't care that. Frankly, I actually like less audience because it's easier to share crazy ideas. I also embedded the posts into my blog's [Snippets page](/snippets). I wanted to post some short random thoughts and ideas to my Blog long time ago, but didn't do it because it's hard to make the post feature mobile phone available. I don't like embedding Twitter. But I'm happy to do it with Mastodon because I have fully control. I tried to find a promising html widget but failed. So I wrote some Javascript with Mastodon API to do it. It's really simple and surprisingly looks well because of the consistent look with other parts of my Blog. It has less than 100 lines of pure Javascript code. You can find the [source code at my Github](https://github.com/wb14123/blog/blob/master/jekyll/snippets.html). I also migrated all my Twitter posts to Mastodon with the publish date. You can also find the [tool on my Github](https://github.com/wb14123/twitter2mastodon).

At last, this doesn't mean I will totally abandon Twitter. I will still follow some people on Twitter. But maybe not with Twitter itself but with RSS. I will also post something on Twitter if I want broader audiences, e.g. the updates of my blog.

## Final Thoughts

I really like the decentralized Internet. That's why I keep my own blog and don't want to write articles on third-party platforms. But the monopoly of big companies are making the decentralized Internet dying. Not only data is controlled by these big companies, the privacy is also threatened.

I'm happy that more and more people are realizing this. And there are more and more people are writing software for decentralized Internet. Other than the Blog, I also host my own file sharing service with NextCloud and instant message service with [Matrix](https://matrix.org/). And after I spent less time on the centralized platforms, I feel I have more control over my time, and I find more interesting people and inspiring ideas.

My blog still has an very important part that's controlled by other company: the comment. It's powered by Disqus. I've lost some comments when I made some changes for my blog. And it also requires login to comment. So I have plan to replace it. I'm still trying to find a good alternative for this.
