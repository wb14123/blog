---
layout: post
title: Improve Books Section of My Blog
tags: [blog, book, reading, life]
index: ['/Projects/Blog']
---

In my blog, I have a page called [Read](/read.html) to keep the books I've read. At first, I used an API from [Douban](https://book.douban.com) (a Chinese social media that has reviews and metadata of books, movies, TV shows). I was really happy about it since it's easy to find and add books into a list on Douban, while still being able to render them in a custom layout. But a few years ago (cannot remember the exact time), the API became unavailable without an API key, and it's impossible to find a way to get one. So I moved my reading list to Goodreads.

Instead of using an API to render the books with a custom layout, I linked the page directly to Goodreads' book collection I created. It was the fastest way to recover the page. Goodreads has a few pain points including the lack of info for some Chinese books. But it was acceptable to me. I thought I could do the custom render with API in the future, only waiting for Goodreads to close its public API as well. Since then, I always want to migrate the page into something else, but didn't really have the time until recently.

You should already be able to see the finished work when clicking the "Read" link at the top of the page. In this article, I'll talk about how it's implemented and the thoughts behind it.


## Script to Download Book Information

Instead of migrating to another platform and use APIs to render the page, I think it's better to take control of the data this time. So I wrote a Scala script to download the book information from websites like Goodreads and Douban. It saves the information into a YAML header of a Markdown file, which is a format Jekyll can read and use in a template. Here is an example of the output:

```yaml
---
layout: book
title: "When America First Met China: An Exotic History of Tea, Drugs, and Money in the Age of Sail"
authors: ["Eric Jay Dolin"]
isbn: "9780871404336"
cover: "https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1337692965i/13812169.jpg"
year: 2012
external_links:
  goodreads: https://www.goodreads.com/book/show/13812169-when-america-first-met-china
  date_read: "2025-06-22"
---
```

The script is available in [my blog's repo](https://github.com/wb14123/blog/blob/master/add-book/add-book.sc). With the command like

```bash
./add-book/add-book.sc <book_url> jekyll/_books
```

It can download the book info files into the target directory `jekyll/_books`. Currently it can take a URL of a single book from Goodreads, or a book collection link from Goodreads, or a single book link from Douban.

## Render Book List with Jekyll

Once the book information is put into the blog's folder, it's easy to let Jekyll recognize it and render it.

First let's add the folder as a collection in `_config.yaml`:

```yaml
collections:
  books:
    output: true
```

After this you can use the variable `site.books` to get all the files under `_book` directory. And you can use each field in the file like `title` and `authors` as well. The template for the "Read" page is in Github repo's [read.html](https://github.com/wb14123/blog/blob/master/jekyll/read.html).

With `output` set to `true`, it will also create a page for each of the books. If only showing the list of books is needed, `output` above can be set to `false`. But this option gives me a new idea: why not also write my reading notes in each book's page?

## Book Reading Notes

I've long wanted to write some notes on the books I've read on my blog. But most of the time it's just not rich enough to become an article. I was also really hesitant to write it on third-party websites like Goodreads or Douban. So a lot of the thoughts just got lost.

With this new script and folder format, it gives me a new idea to write the book notes in a separate section of my blog, separated from my main blog posts, much like what I have done for the [Snippets](/snippets) page which are shorter random posts powered by Mastodon.

It's very easy to do that technically: just write the notes in the same file after the `---` separator of metadata. With `book.output` set to `true` like in `_config.yaml` above, it's just a matter of adding a new layout template like [\_layout/book.html](https://github.com/wb14123/blog/blob/master/jekyll/_layouts/book.html) to render the notes pages.

I don't think it's worth to write notes for every book. So in the read list, I only add a link to the note when there is one. The individual book pages will still be created by Jekyll even without note content but I'm okay with it since the users don't see them. I also show how long the note is in the book list, so that it can save the visitors some time if they only care about in-depth notes.

I don't have the interest to backfill the notes for all the books I've read, so I only did it for the recent ones. I was thinking about to just write a few sentences for each book, but it surprised me how much and how in-depth I've written at the end. It's also very fun to write since it's very casual. It makes me realize I truly lost a lot of thoughts by not having a place to write book notes. Hopefully I can read and write more in the future and provide more insights to other readers.

## Other Considerations

I thought about how the visitors of my blog can find the updates of the book notes. I don't think it deserves to be listed in the index page (currently has all the posts in published time order) since it makes the writing less casual. Probably the best option is to have a separate RSS feed for them.

I'm also considering how to best organize the books I'm reading, and books I've read a little bit but abandoned. It's valuable to keep notes on the book when I'm still reading but not sure if it has value to be shown here on the website. Maybe it's a place for my private notes and I can publish it once I finished the reading. For the books abandoned, maybe I can keep a special mark in the book info and has some short comments in the notes.

