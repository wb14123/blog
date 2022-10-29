---
layout: post
title: Use RSS and Kindle to Read News
tags: [technology, RSS]
index: ['/Computer Science/Digital Life']
---

*Update: check [this article](/2022-10-29-RSS-Brain-Yet-Another-RSS-Reader-With-More-Features.html) to see how to use a RSS reader I wrote to take back the control of your information consumption.*


In a [previous blog post](/2020-08-02-What-Is-Wrong-abount-Recommendation-System.html), I talked about one of the reasons I want to use mobile phone and social network less often. In this article, I will talk about one of the strategies of doing that: don't read news on mobile phone or computer. Use Kindle or other E-Readers instead. Here is how I do it:

## Use RSS to Get News

RSS was so popular before Google Reader was killed. You can choose the sources by yourself instead of some black box recommendation algorithms. I talked about what's wrong about the recommendation system in [a previous post](/2020-08-02-What-Is-Wrong-abount-Recommendation-System.html). RSS is also an open protocol, so you can choose whatever software you want without vendor lock-in.

RSS is open and flexible, so people have various ways of using it. As a person who only wants to know important news and doesn't want to lost in the overwhelming information, I subscribe to a few high quality sources. Basically 1 or 2 sources for local news, global news, tech news and culture. That's enough for 1~2 hour's daily reading. For the news sources, they should be focus on facts instead of its own opinion, and should cover the whole story instead of partial fact. With limited sources, I can focus more on the whole article instead of a lot titles.

Many websites doesn't provide full article RSS output. Some doesn't even provide RSS at all. But as long as the content is available online, we can use tools to crawl and convert it to an RSS feed. There is a tool called [RSSHub](https://github.com/DIYgod/RSSHub) does exactly this. I also contributed some code to it.

## Use Calibre to Send RSS Feeds to Kindle

Kindle doesn't have any RSS reader. So once we have the RSS sources ready, we need to import them. There were a lot of platforms to do that, but most of them are not reliable anymore. Luckily, we have an even better option now: [Calibre](https://calibre-ebook.com/). It's a very powerful e-book management software. Most importantly, it's free and open source. So you don't need to give any information to anyone else.

The feature is available at "Fetch News" in Calibre's menu. It turns the RSS sources into an e-book and send it to an Email address (Most E-readers, including Kindle, allow you to transfer books by sending an Email). It has many pre-defined sources but you can also add custom RSS sources. You can do it manually or import an OPML file. It can be configured to send the e-book daily or on specific days in a week. The only downside is you need to leave it running in the background. But since it doesn't collect your data secretly, it barely uses any resource in the background.

## Use Kindle (Or Other E-Readers) Instead of Phone or Computer

I got a Kindle after I graduated from university. I find it's the most useful thing I've bought in that price range. I read much more books because of it. It's so easy to take so I usually read it during commute. (My readings dropped a lot after I started working from home). And the screen likes paper so it doesn't hurt my eyes. Some people may find low fresh rate E-Ink display is hard too use, but it's perfect for book reading. And just because of that, I can focus on reading instead of watching videos or play games.

After I started reading news on Kindle, I find I can read more carefully and completely. Though I spent much less time on reading news, I don't feel I missed anything. Maybe the only things I missed are the rumors without fact check and click-bias content. I feel I'm still connected to the world, but with a much more peaceful mind.
