---
layout: post
title: Jekyll Plugin to Load Asciinema Recordings Locally
tags: [Jekyll, Blog, command line, Asciicast]
index: ['/Projects/Blog']
---

[Asciinema](https://asciinema.org/) is a wonderful tool to record Linux terminal. It saves the records as a text format called Asciicast. However, it has a strong integration with its website. Especially if you want to embed the recordings into the web page using some simple JS code like this:

```html
<script src="https://asciinema.org/a/14.js" id="asciicast-14" async></script>
```

You need to share the recordings to Asciinema's website and need to link an account with the recordings, otherwise they will be deleted after 7 days, which I just found out yesterday. I don't want my blog to rely on some third party website for core content, so I need a way to load the recordings from my website itself.

Luckily, the [Asciinema Javascript player](https://github.com/asciinema/asciinema-player) is open source and supports loading recordings from a URL out of the box. First you need to import the CSS:

```html
<link rel="stylesheet" type="text/css" href="/asciinema-player.css" />
```

This is no big deal since this can be put in Jekyll's template. Then you need some JS code like this:

```html
<div id="demo"></div>
 ...
<script src="/asciinema-player.min.js"></script>
<script>
  AsciinemaPlayer.create('/demo.cast', document.getElementById('demo'));
</script>
```

It's a little bit too much for embedding a terminal recording in a blog. However, with the powerful Jekyll plugin system, we can write a plugin to make it simpler so that we can just use a tag to include it:

```
{% raw %}
{% asciicast <id> %}
{% endraw %}
```

Here is the implementation, it's also in [my blog's Github repo](https://github.com/wb14123/blog/blob/master/jekyll/_plugins/Asciicast.rb):

```ruby
module Jekyll
  class RenderAsciicastTag < Liquid::Tag

    def initialize(tag_name, text, tokens)
      super
      @text = text.strip
    end

    def render(context)
      "<div id=\"cast-#{@text}\"></div>" \
      '<script src="/static/js/asciinema-player.min.js"></script>' \
      "<script>AsciinemaPlayer.create('/static/asciicasts/#{@text}.cast', document.getElementById('cast-#{@text}'), {rows: 10, autoPlay: true});</script>"
    end
  end
end

Liquid::Template.register_tag('asciicast', Jekyll::RenderAsciicastTag)
```

It will find the recordings under `/static/asciicasts/{id}.cast` and load from there.

Put this file under `_plugins` and happy hacking!
