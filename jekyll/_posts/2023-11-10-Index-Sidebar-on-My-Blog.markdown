---
layout: post
title: Add Index Sidebar to My Blog
tags: [blog, Jekyll, Javascript, desgin]
index: ["Projects/Blog"]
---

In a previous blog [Add Index to My Blog](/2021-10-31-Add-Index-to-My-Blog.html), I talked about how I added an index page to my blog that put all the articles into categories. I always wanted the index to be a sidebar instead of a single page, but I guess I didn't wrap my head around about how to implement so I gave up at last. But recently, when I started to use [Obsidian](https://obsidian.md/) and checked some demos of [Obsidian Publish](https://obsidian.md/publish), I found having a sidebar is so useful and beautiful so I decide I should implement it.

You can see the result right now: if you are on a big screen device, the index is on the left side of the page. If you are on a small screen device like a mobile phone, it will show a menu button at the top left corner instead. Clicking it will take you to the index.

When I implement it, I want to keep it simple and stupid. That means:

* I want to be as simple as possible as long as it has the function: show articles in nested categories.
* I want to use as little Javascript as possible so people can still use it with Javascript disabled.

I found the design of Obsidian Publish is very good. So I copied lots of details from them with some modifications: I didn't implement showing/hiding sub items when click on the index entry since I think it's not necessary, and I like how it looks when all the articles are listed there: feels like I've written lots of things. The categories are sorted by alphabet order and the posts are ordered by publish date. I also added the publish year for each article entry: some articles can look outdated but if people noticed the published year they can understand the context.

Since I'm using Jeykyll, I can generate plain HTML when possible to avoid the usage of Javascript. So the sidebar is generated for each page instead of using Javascript to keep the sidebar and replace the article content on the fly. Javascript is only used for 2 features:

1. Remember the position of the sidebar when jump pages.
2. Scroll the sidebar to show the entry for the current page if it's not in the viewpoint.

Both of the features are not that important so the sidebar is still usable without Javascript. Even for the menu button on small screens, it's not popping up a dialog. It just jumps to a new static page that has all the index so no Javascript is needed.

The previous implementation of the index page uses recursive templates: Since the nested index is a tree, rendering the content in a recursive manner is a nature thought. However, I made that mistake to put the complex logic into the template engine. So this time, I traverse the tree with Ruby code and generates a list for the template to render. It has all the information like entry type, the depth of the entry and so on. It makes the template code much simpler so it's easier to implement other features on top of it.

If you want to checkout the detailed implementation, go to my [Github repo for the blog](https://github.com/wb14123/blog) and check [`jekyll/_plugins/Index.rb`](https://github.com/wb14123/blog/blob/master/jekyll/_plugins/Index.rb) and [`jekyll/_includes/index_menu.html`](https://github.com/wb14123/blog/blob/master/jekyll/_includes/index_menu.html).
