---
layout: post
title: A 2-Year Reflection for 2023 and 2024
tags: [life]
index: ["/Life"]
---

Here is another new year! I didn't write any retrospective for 2023, so I'd like to combine it with 2024 together in this one since 2023 is a very important year for me. I planned to write it after I came back from [China's travel](/2024-03-19-Travel-Back-to-China.html) but never got the time. When I finally got the time it was already too late to write a year-end review. I'm staying in China again now (when I first started this article but I've been back when publishing it). While there is no work related thing to worry about when on vacation, I can use this time to review what happened in the past 2 years so it won't slip through again.

## A New Life

The most significant event happened in the past 2 years is that I have a daughter. Having a baby changed my life so much that it felt overwhelming at first. I always tried to keep my life simple and predictable, but a baby is the contrary of that. Before having the baby, I thought raising a baby is a science. While it is true on some level, it is also a kind of art. In theory, it's about meeting the baby's needs by observing, like feeding the baby, changing the diaper and so on. But in reality, there are lots of guess work about what the baby needs when she is crying, and it's very stressful if you cannot figure it out. And sometimes she just cries for no reason (e.g. baby colic). Also sometimes you know what needs to be done, but it takes so much energy to do it. For example, in the first months, it's really hard to get any good sleep and it takes a big toll on everyday life and the mental health.

However, after a few months, especially once the baby is more than 1 year old, it's really rewarding. Once she can babble words, run all over the place, and express her cares about me, I feel everything is worth it. It's such a special feeling that knowing you are raising a new life and be responsible for it. Because of that, I feel like my life is more meaningful and I value it much more than before.

## The Old Project: RSS Brain

On the other hand, my old project [RSS Brain](https://rssbrain.com) continues to get improvements. I'm so happy about this project because I can put theory into real world after I learned something. And I continue to use it everyday to get information from the Internet. Now I cannot imagine my Internet browsing without it. The feature set and backend is mostly stable in 2023. At the beginning of 2024, I started to refactor the frontend from Flutter to web tech. Mainly using htmx and Alpine.js with Scala to generate the html code from backend. This is such an important decision for this project and it turned out perfectly. Not only it feels native on the web platform, the code is also much cleaner since it's mostly Scala now -- the same as the backend. The performance is also much better since most of the html is rendered from the server so the client needs little power compared to a Flutter one. On the way I also developed some frontend libraries for the project's needs and wrote some blogs.

In the middle of 2024, I started to release the code of this project on a regular basis: I feel like the project is so cool that it's a shame if only I can see the source code. There is still room for improvement on that side since there are still lots of doc missing, and it uses some other of my open source libraries that may not be released very properly. It would be great to clean it up so it's easier to build and run on a fresh machine.

There are also some UI and quality of life features that can continue to be improved. This would still be a project I continue to work on and release on a regular basis. Sometimes working on these small features is more like gardening, which makes me feel peaceful and relaxed when too many other things are happening in the life.

I may also add more AI features. I know AI is kind of a hype word now but that was the plan from the beginning. The name RSS Brain is meant to have some analysis features for the feeds so you can have more insights from them. The features it has now like search filters, related articles are like this. With the advance of the LLM, more things are possible and I'm pretty excited about. But before that, I need to find some proper infrastructure to do that, which will be discussed in the following sections about databases.

## Distributed System Infrastructure

With RSS Brain as a use case, I try to setup my own infrastructure for a high available system. It took shape in 2023 and the final version can be found in [this blog post](/2023-11-28-Introduce-K3s-CephFS-and-MetalLB-to-My-High-Avaliable-Cluster.html). It works really good that I didn't change much things in 2024. However there is a very important part that's still missing: the upgrade of the system components. Since the services and even some infrastructure like CephFS are running in containers, there is no single command you can run to upgrade all the packages. Some images can be very outdated that there are some security risks. I meant to write some tools for upgrades but didn't find the time for it. I suspect I wouldn't have time for that in the coming year either but just list it here so that I don't forget.

## In Search of Databases

There are 2 things motivated me to search for a new database: the license change of CockroachDB and the needs of vector similarity search. This work took lots of my time in the second half of 2024. For the first point, I talked it briefly in the blog post [Jepsen Test on Patroni](/2024-12-02-PostgreSQL-High-Availability-Solutions-Part-1.html), which I'm very proud of. I may write another blog post about it too so I will save the details for the future.

For the vector database, I think anyone that follows the AI trend recently would be all too familiar with that. There are 2 use cases for my projects: recommendation for RSS Brain using embeddings, and general RAG for LLMs.

I talked about the recommendation use case in the blog post [Update on RSS Brain to Find Related Articles with Machine Learning](/2023-11-14-Update-On-RSS-Brain-to-Find-Related-Articles-with-Machine-Learning.html): it's now using Elastic Search for vector similarity search, but the performance is not so good with my less powerful cluster. So I'm searching for some other solutions that have a lower memory footprint. And if possible, use it for full text search as well so that I don't need to run Elastic Search at all.

About general RAG for LLMs, I've been interested in neural network based machine learning back in 2015. In my [2017 retrospective](/2018-01-02-The-Year-of-2017.html), I talked about the AI project I was working on. That's when I read the paper *Attention is All You Need*, which is the foundation of the nowadays LLMs. I always wanted to develop some RPG game with human like AI and it seems finally to be technically possible. Along with some other tools I want to develop with LLMs in the coming year, a database that is capable for vector search is a must have. I've already done lots of research on that area but didn't feel like I reached to a point to share it. So I'll save it for a future blog post as well.

## Blog Posts

In 2023, I [added an index sidebar to my blog](/2023-11-10-Index-Sidebar-on-My-Blog.html). It works really well. The blog posts in the past 2 years not only have better structure, but the quality is also better. My perspective has changed when I write a new blog post: instead of a one-off writing and maybe something I'll not read again so much, the index structure makes me feel like I'm filling some holes in my knowledge graph, and it's a forever ongoing project so I take more care about it. And like I mentioned in the previous blog post above, the structure also makes me aware what areas I'm focusing on so that I can adjust it if it's not consistent with my goal.

## Retro Hardware

I always liked the aesthetic of some old hardware. The mechanical parts instead of a flat block with touch screen makes the design much more interesting and fun to use. In the past two years, I bought some retro hardware and it became a hobby:

In the first months of having my daughter, she had reflux so I needed to hold her to sleep very often to avoid spitting up milk. In order to make the time easier, I bought a retro mini handheld game console [Miyoo Mini+](https://officialmiyoomini.com/product/miyoo-mini-plus-v2-official-store-sale) so I can play games while holding her to sleep. It can simulate lots of games up to PS2 era. It opened a new world to me because there are so many great games from the past that are still fun to play nowadays. In the past, I've already realised good games are not only good graphics. If we read classical books from many years ago, why shouldn't we treat classic games like that? After flashing the custom ROM [OnionUI](https://github.com/OnionUI/Onion), I can also write some modern retro games for myself with platforms like [TIC-80](https://tic80.com/). The only complaint is it doesn't have bluetooth built in so I cannot use my Airpods (another device I bought to make the time of holding the baby easier) with it.

Another big part of the retro hardware is related to radio: I bought some AM/FM analog radios, wideband digital radios, as well as software defined radio devices. I got interested in it after reading the book [The Knowledge: How to Rebuild Our World from Scratch](https://www.goodreads.com/book/show/18114087-the-knowledge). It just feels so fun with the idea that you can send and capture information through the air. Technically modern technologies like mobile networks and WIFI is still sending info through air, but the digital signal and too many layers added makes it less fun. Additionally, when it's easy to find specific things you want to consume nowadays, it's very relaxing to consume passively like the old time TV channels. That's kind of the idea I described in the blog [Random Playlists for Self Hosted Videos](/2024-06-03-Random-Video-Playlists-for-Self-Hosted-Videos.html). But even more relaxing with radios because you don't even need to watch anything.

I also bought a second hand Nokia phone [Nokia 5730](https://en.wikipedia.org/wiki/Nokia_5730_XpressMusic). It's very similar to my first phone Nokia 5320, but with a slide out keyboard. I wanted to play some old Symbian or Java mobile games but didn't get much chance. It may stay in the drawer for a longer time because I have lots of other retro plans in the new year.

At last, I got a Thinkpad X220. This may be worth a blog post on its own (again) so I will just be brief here. This is the laptop I always wanted since I graduated (X200 before that). The company I was working for gave us Macbooks, which you cannot really complain. In the following years, I also bought Macbook as my personal laptop because it's very hard to find the same level screen on other laptops. In recent years, there are multiple times I wanted to buy a used one for various reasons, but didn't do that mainly because of the price. I finally did it in the last year since I found the second hand market in China is much cheaper than the ones in north America. So I got one pretty cheap and I'm surprised how capable it still is. I meant to play some old Windows game on it but it seems to be such a waste so I installed Linux on it. It works so well that I'm actually taking it for travel instead of my Thinkpad X1 Extreme (gen1).

About old PC gaming, I dual booted Windows on the Thinkpad X220. But then I found my old Thinkpad E40 at my parents' place. So I will use the Thinkpad E40 for the purpose instead, which is pretty appropriate since it's the first laptop I had. I played many games on it and there is no better device if I want to play more games from that era.

## Conclusion

Overall the life is more structured even with a new baby in my life. One factor may be I've moved to Toronto for a while and I'm used to the routine. The end of Covid is another big factor to make not only me but a lot of other people's life back on track. I think a sentence in [one of my poems](https://mastodon.binwang.me/@wangbin/112794830887081984) encapsulated my past 2 years pretty well but it's hard to translate to English:

> 抱朴寻微末，结庐草太玄。

"抱朴" (Bao Pu) is referred to the classical Taoist text [抱朴子](https://en.wikipedia.org/wiki/Baopuzi). Literally, it means "hug the simplicity". "太玄" (Tai Xuan) is another famous text in Chinese history [太玄经](https://en.wikipedia.org/wiki/Taixuanjing). Literally, 太 means "too", "supreme" or "great". 玄 is a very special word in Taoist, which means mysterious, profound or difficult to understand. The author Yang Xiong spent lots of time to write it but it doesn't really have any practical use and his work was not very mainstream in his era. So the sentence above can be roughly translated into:

> Embracing the simplicity, I seek the profound and subtle.
>
> In a humble hut, I write Tai Xuan in solitude.
