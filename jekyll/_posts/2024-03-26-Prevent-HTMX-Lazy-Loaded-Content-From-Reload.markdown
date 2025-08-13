---
layout: post
title: Prevent htmx Lazy Loaded Content From Reloading
tags: [htmx, web, UI, Javascript]
index: ['/Computer Science/UI/Javascript']
---

This is a short article about some tricks in [htmx](https://htmx.org). I have more to say about htmx but I'll save that to another blog. In this one, I will skip the basics about htmx and assume you already know that.

## 1. Problem

I'll briefly introduce two features of htmx in order to explain the problem. You can go to official website for more details about the features.

### 1.1. Browser History

htmx has [a feature to interact with browser history](https://htmx.org/docs/#history). Here is an example in the official document:

```html
<a hx-get="/blog" hx-push-url="true">Blog</a>
```

This will change the url in browser to `/blog` when you click the link and save a snapshot of current page into local storage. When you click the back button in browser, htmx will try to find the cache in local storage, and swap it out so you don't need to reload the whole page.

### 1.2. Lazy Load

htmx sends requests when an event is triggered on an element. The rule is defined by [hx-trigger](https://htmx.org/attributes/hx-trigger/) attribute. There are some special events that can be used for lazy loading:

* load - triggered on load (useful for lazy-loading something).
* revealed - triggered when an element is scrolled into the viewport (also useful for lazy-loading).
* intersect - fires once when an element first intersects the viewport.

However, when combining this with history support, the lazy loaded elements will be requested again when the pages are navigated in history. Here is an example:

```html
<a hx-get="/page1" hx-push-url="true" hx-target="#content">page1</a>
<div id="content" hx-get="/content" hx-trigger="load"></div>
```

When you click on `page1`, it will replace `#content` with the response from `/page1` and change the URL. However, when you click the back button in browser, htmx will send a request to `/content` again even though it's already in history cache, because technically, `#content` **is** loaded again so `hx-get` is triggered based on the `hx-trigger` rule. This results in a waste of resources and can sometimes make the webpage lose its previous scroll position.

In this article, I'll show some tricks to prevent this. They are very simple once you know them but sometimes it's just hard to get when you are new to the framework.

## 2. Best Solution: Swap Outer HTML instead of Inner HTML

I think this is the best solution. It's so simple that I don't know why I didn't get it earlier. Anyway, that's why I write this blog so that it can help more people like me.

By default, htmx swaps the inner HTML of the element. So the `hx-trigger="load"` attribute is still there after the content is loaded and will be triggered again when loading from history. The solution is to just let htmx swap the outer HTML instead. Using the same example, the code will be changed to this:

```html
<a hx-get="/page1" hx-push-url="true" hx-target="#content">page1</a>
<div id="content" hx-get="/content">
  <div hx-get="/content" hx-trigger="load" hx-target="this" hx-swap="outerHTML"></div>
</div>
```

In the new implementation, we have another `div` tag inside `#content` to do the lazy load. After the response is loaded, it will swap out the whole `div` element so `hx-get` and `hx-trigger` are not there anymore when the snapshot is taken and loaded from history.

As I said, this is the best solution in my mind and I think it fits all the cases. So if you only care about the solution, you can stop reading here. I record the following solutions simply because I figured them out earlier than this one.

## 3. Solution B: Don't Snapshot the Whole Body

The solution above removes the htmx attributes. The solution in this section tackles the problem in another direction: it prevents the element from loading again when going back in history.

By default, htmx will take the snapshot of `body` and put it into history cache. That's why when going back in history, the `load` event of the element is triggered again. To prevent it, we can let htmx only snapshot children of `#content`. [Here](https://htmx.org/docs/#specifying-history-snapshot-element) is the official doc about how to do it. Using the same example, the code will be changed into:


```html
<a hx-get="/page1" hx-push-url="true" hx-target="#content">page1</a>
<div id="content-load" hx-get="/content" hx-trigger="load" hx-target="#content"></div>
<div id="content" hx-history-elt></div>
```

Here we load the content with `#content-load` element. htmx will only swap out `#content` when we go forward or back in browser history since we added `hx-history-elt` on `#content`. This prevents `load` event from being triggered on `#content-load` so it will not send a new request.

But this solution has great limitations: you need to change the snapshot element which is not always possible.

## 4. Solution C: Remove htmx Action Attributes Before Taking Snapshot

This is a solution that could work in theory but I didn't test it, because I came up with the best solution when thinking about it.

The idea is similar: we don't want htmx action attributes like `hx-get` when we load the history. Other than swapping the whole outerHTML, there is a htmx event you can catch in Javascript to remove the attribute before taking a snapshot:

```javascript
htmx.on('htmx:beforeHistorySave', function() {
  document.getElementById('#content').removeAttribute("hx-get")
})
```
