---
layout: post
title: "RSS Brain: Yet Another RSS Reader, With More Features"
tags: [RSS, project, RSS Brain, digital life, news]
index: ['/Projects/RSS Brain']
---

I've written yet another RSS Reader called RSS Brain recently. If you just want to take a quick look at the main features and try it, [the official website](https://rssbrain.com/) is the best place to start. Though most of the main features are already finished and I've used it personally for a few months, be aware it's still in beta stage and hasn't been tested by a larger group yet. So please let me know if you run into any issues. I will open source it as well in the future (for non commercial usage) but it's not ready yet for now.

In this article, I'll give an introduction to the motivation behind it. You may have a better reason to try it after reading this.

RSS is just a protocol to aggregate news. But how to organize them depends on the RSS reader.  While lots of RSS readers have the basic organization features like folders to manage the feeds, it is not enough. Not a lot of them have taken new technologies from fields like information retrieval and machine learning. Even a few of them have done that, they are using some complex and black box algorithms which we don't know what is going on behind the scenes. I've written [an article](/2020-08-02-What-Is-Wrong-abount-Recommendation-System.html) that talks about the harm of letting black box algorithm decide what we read. So in RSS Brain, I use transparent algorithms to help organize the feeds and articles without use clicks or read time as the optimization target, so that we can have a good understanding about why it behaves in that way, and in that way we can decide whether it's good for us or not.

Here are some pain points in the traditional RSS readers and how I solve them in RSS Brain with transparent algorithms.

## 1. Traditional RSS Readers Don't Rank Articles by Weight

The mainstream RSS protocol doesn't have a field to indicate how important an article is in a feed. It is very different from news papers or news websites, which have headlines for the most important news. When it comes to RSS, there is no difference between the importance of the articles. It is a smaller problem when there are not so many articles in a feed, but for feeds that have lots of articles, or forums like Reddit and Hacker News, that is a very big problem.

Reddit and Hacker News sort the articles by both votes and timeline. The algorithm is relatively transparent (although less and less transparent in the case of Reddit). Some people don't want the rank to be affected by other users at all, but I'm okay with this kind of ranking for these reasons:

1. The posts are just too many to read them all without some kind of priority.
2. The community is part of the forum. If I like a subreddit, it means I trust the community and mods to promote high quality posts. Otherwise I will just not subscribe to it.
3. If you think about it, the traditional media also rank news for you, even though it is selected by more professional people. But like I said in point 2, if you trust the community of a subreddit, then it doesn't make a big difference.

### 1.1 Ranking Algorithm

Some readers have ranking algorithms to sort the articles for you, but those are mostly black box algorithms, which is harmful like I said before. In RSS Brain, we will take the votes from the source, and sort it with an algorithm that is similar to Reddit:

$$ S_{vote} = log_{10}v $$

$$ S_{time} = { time \over C } $$

$$ S = S_{vote} + S_{time} $$

Where `v` means how many votes this article has. `time` means when this article is posted. `C` is a constant number that indicates how much time contributes to the whole score. I'm using 12.5 hours in my implementation.

For $$S_{vote}$$, it means the first 10 votes will get the most weight, the next 100 votes have the same weight as the first 10 votes, and so on. It's not a perfect algorithm but works well enough, and is easy to implement. Most importantly it's very easy to be understood.

Once you have the score, you have the option to sort the articles in a folder or feed based on score instead of time.

![ranking](https://www.rssbrain.com/images/sort_post.png)

### 1.2 Data Source

Another obvious problem is how to get the votes. The traditional RSS protocols don't have a specific field for vote count, luckily atom protocol has the ability to extend it with custom tags. So RSS Brain will parse these tags in `<entry>` tag if exists: `<*:comments>`, `<*:upvotes>`, `<*:downvotes>`. `*` can be any namespace. I've added [these fields](https://docs.rsshub.app/en/joinus/quick-start.html#submit-new-rss-rule-code-the-script-produce-rss-feed-interactions) to a very popular RSS generator [RSSHub](https://rsshub.app/) in namespace `RSSHub`. So if someone includes these fields when implementing RSS, RSS Brain will be able to parse it and use `upvotes - downvotes` as the votes. If they are not available, RSS Brain will try to use `comments` instead.

Reddit and HackerNews are two of my main daily reading websites, and I believe it's the same for many other people, so I also included an implementation to fetch the posts from Reddit and HackerNews JSON API. You can just input HackerNews or subreddit URL when adding a new source, it will have an option to use the JSON feed when it tries to find the feeds. I know it's not a very standard way, but it's easier for me to implement rather than do it in RSSHub. Once there is a RSS implementation that contains the fields above, you have no trouble to use the RSS feed.

Since RSS Brain is parsing the tags in the RSS feed, for data sources other than forum, the RSS generator can also try to generate the votes in some way if necessary. For example, some score based on the article position of the website, the font size and so on. I haven't done any experiment with it yet but I think it's an interesting idea to explore.



## 2. Filter Articles With Search Terms

Ranking the articles is one way to get the interesting articles to pop up. Another way is to filter the articles: sometimes we only care about a topic in a feed. In traditional RSS readers, there is no easy way to filter the topic out if there is no feed for that topic. In RSS Brain, you can define a search term on a folder, so that when you check the articles in this folder, it will only show the articles that match the search term.

For example, you've got a few news feed, but only care reports about the war between Ukraine and Russia, then you can just set the search term as `"Ukraine" AND "Russia"` on the folder and enable search filter. After that, when you click on the folder to see articles, it will only show the articles about those news.

![filter folder](https://www.rssbrain.com/images/filter_folder.png)

By the way, you can also just search in a folder or source. This is very useful for me to search some news, since the search quality of search engines for some news is very bad, especially for Chinese news, where very suspicious news agencies are always shown in the top results.

## 3. Show Related Articles

This is a feature I found to be pretty interesting when Google News included it. But Google News selects the news source for you. In RSS Brain, you can get related articles from the feeds you subscribed to, so that you can decide which sources are valuable to you instead of letting a big corp decide that.

Currently the implementation only has the ability to select from all your subscribed feeds. But even with this implementation, I find it is very useful in some ways:

* When I check the news, the related articles will show a post related to this in forums like Reddit and HackerNews, so that I can check other people's opinion.
* I can compare the report coverage from both left and right news agencies, to get a whole picture. I don't use this feature a lot myself, but I know a lot of people like that and there are some popular apps do just that.
* I usually read news from a high quality source. But if I find some news to be interesting, I'd like to read more coverage on that. That coverage doesn't need to be high quality but might have more details.

I have plans to extend this feature. (*Update: this feature has been implemented. Check the [blog post here](/2022-12-03-How-RSS-Brain-Shows-Related-Articles.html).*) It will be much better after that: RSS Brain will support related articles groups, so that you can show related articles not only from all the subscriptions, but also from the folders of your choice. So you can configure it to show related forum posts in one group, left/right news coverage on another group, and so on. I'll also implement a feature to show related articles from a time range, so that it can filter the matches based on time, to make the news more related if you want.

## 4. Just Want to Write Something

Last but not least, I just want to write something and feel the happiness of coding. I really enjoy myself a lot while writing this project: coding, choosing the tech stack to use, setting up high availability cluster and database, and so on. Everything is so elegant because I can decide how to do it.

While enjoying myself during the development of the project, I find the product is pretty useful as well. So I decided to share it, and if it benefits more people I'm happier. As I said at the beginning of the article, I'll open source the whole project and allow non commercial usages when it's ready. But in the meantime, you can pay a monthly fee to use the software. The reason for this payment mode is twofold: I don't want this software to be flooded with terrible ads, but I still need some revenue to keep the infrastructure running (for both hardware cost and my time). So I want to set some barriers at first to limit the user base, so that the users can have a better experience. Hope you enjoy this app and let me know if you run into any issues or have any question.
