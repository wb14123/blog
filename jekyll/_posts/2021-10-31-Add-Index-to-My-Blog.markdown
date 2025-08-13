---
layout: post
title: Add Index to My Blog
tags: [blog, index]
index: ['/Projects/Blog']
---

*Update: the index page has been removed in favor of the index sidebar. See [Add Index Sidebar to My Blog](/2023-11-10-Index-Sidebar-on-My-Blog.html) for more details.*

I added an [index page](/index_page.html) to my blog yesterday, which can also be accessed from the "Index" entry at the top of every page. When I moved this blog to Jekyll, I [removed categories](/2012-12-10-Remove-Categories.html) since I thought tag should be enough. But after a few years, I feel something is missing. One of the reasons maybe I never implemented find posts by tags, which makes tags useless. Even if I had that, I feel it's still not enough. But I don't want categories, since "category" in Jekyll normally means a flat level structure. What I want is a nested category structure. It should let me create as many levels as I want, like the index in a book. In this way, I will have a better idea about which field I have covered, and what I should focus on based on my future plan. It can also benefit the readers: they can find the posts they are interested in much easier. Not every blog post has the same quality or depth based on the nature of the topic, so in this way the posts under most interesting topics can be grouped together instead of being buried in the timeline.

To implement this feature, I wrote my own little plugin. I only knew some Ruby knowledge from one of Ruby's author's books (I read the Chinese version of the book and cannot find the name of the English version, I believe the original book is published in Japanese with the name まつもとゆきひろ コードの世界~スーパー・プログラマになる14の思考法). I never worked on any project with Ruby. But I always wanted to write some plugins for Jekyll so I know I can customize my blog in a better way. Luckily this task is simple enough. The result source code is on my Github of this blog repo. It has a [ruby script](https://github.com/wb14123/blog/blob/master/jekyll/_plugins/Index.rb) to generate the index page from post property, with a [template](https://github.com/wb14123/blog/blob/master/jekyll/index_page.html) that uses another [recursive template](https://github.com/wb14123/blog/blob/master/jekyll/_includes/index_page.html).

So here is how this plugin works: the only thing I need to do while writing a post is to add a new property called `index` at the start of the Markdown file, like this:

```
---
# these three properties are needed before this feature:
layout: post
title: Add Index to My Blog
tags: [blog, index]

# this is the newly added property:
index: ['/Projects/Blog']
---

```

Then the plugin will parse `index` field, break it down into multiple levels based on `/` in it, and group all the posts in the same field together. The categories are sorted based on alphabetical order, while the posts in each category is sorted from old to new. `index` is an array, so in theory I can add a blog post into multiple categories if I want to, but I try not to do it if it's not necessary, since it gives a false feeling about how many posts I've written. For example, at the beginning, I tried to have both `/Computer Science/Database` and `/Computer Science/Distributed System`, and almost every post under `database` is also in `distributed system`. Then I decided it doesn't make sense: they are more about distributed system because what I care most in those articles is about database transactions. So I removed the category `database` and put all of them just under `distributed system`. If I write anything like query optimization in the future, I may create another `database` category, but it's not needed for now.

I'm very happy with the result. I want it to be simple enough as a static page without the need of Javascript. Javascript can certainly improve some UI like expand/collapse the levels. I may implement both versions in the future so people can view it with and without Javascript, but the current UI is good enough for me.

UI aside, I'm much happier about the content. The index has a good structure. It shows the fields I've explored. It even shows a correlation between the focus and the timeline, which I never thought about before. For example, the only two posts about algorithm are published when I was in the University, when I joined some programming contest. The posts about Linux were mostly published in the last year of my college life, when I was in the first few years of using Linux and had an internship at Redhat. It doesn't mean I'm not interested in Linux anymore. It's just after so many years of using Linux, it becomes a tool that is so fundamental to my work and digital life, and I don't feel the necessity to actively learn the basic things and tune it. Data processing related topics happened mostly when I worked in an A/B testing company. And machine learning related topics also happened around that time, when I was most interested in neural networks, which resulted in the most popular side project I've built. I started to post distributed system related topics after I joined a database company, and still find it's an interesting topic thus many posts in the last year. Which also reminds me if I want to learn more about it I should continue to write articles about it in this and following years. But not everything is perfect, mainly because I didn't write everything I've worked on my blog. One of the reasons is a lot of things I've explored are already somewhere else and I don't want to just copy something to my blog. The projects I've worked on are also not covered. I'm still thinking what's the best way to put it into the index: either add a link to the project page, or write a blog post about each of the projects. Other than those, there is still something missing. It's mostly because of laziness, which I should change in the future.

So as a result, the index page achieved some of the goals: covering some of my past works and thoughts, and reminding me what I should focus on in the future. About the goal of benefiting the readers, you as a reader decide if it's good or not.


