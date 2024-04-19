---
layout: post
title: Make Flutter Web Apps More Native Like
tags: [Flutter, web, UI, Dart]
index: ['/Computer Science/UI/Flutter']
---

## Background

I've built the client app of [RSS Brain](https://rssbrain.com) with Flutter so that I don't need to write different code for different platforms. It's a pleasant to write Flutter code. And the app works good enough for Android and iOS. However, Flutter web support is a different story. You can feel the app is just not a normal website. I'm not satisfy with that. After attempts to make it more like a native web page and failed, I'm rewriting it with web technology again. That's why the [last blog post](/2024-03-26-Prevent-HTMX-Lazy-Loaded-Content-From-Reload.html) is about htmx.

Before I move on, I'd like to record what I have tried, as a note for myself and hopefully it can also help someone else. It's really sad this article as my first blog about Flutter, maybe the only one for a long time.

## How Flutter Renders a Web Page

In Flutter, you define the UI widgets in Dart. And Flutter the engine will parse the widgets and render it to different targets: iOS, Android, web and even Windows and Linux applications. In principle, I think that is a good idea and I really enjoy writing Flutter code compared to Javascript frameworks like AngularJS or ReactJS. It's really unfortunate the web support is not good enough to me.

The core problem is how Flutter renders the web pages. We all know a web page is represented in HTML. Even if we don't write HTML directly but use a Javascript framework, it is manipulating HTML tags at the end. Flutter renders widgets to different HTML elements like `div` at first. However, it was later changed to draw all the widgets in a canvas. (The old render method is still available through `--web-render html` but I encountered multiple bugs and seems it's given less and less care). This makes Flutter web apps doesn't really behave like a native app, because a normal web page doesn't have everything in a canvas.

For the problems it brings, I found solutions for some of them. For some others, I didn't find one. The sections below are some of the problems and some of the solutions.

## Make Text Selectable

By default, the text in Flutter app is not selectable. You can use the [SeletableText](https://api.flutter.dev/flutter/material/SelectableText-class.html) widget to make text selectable.


## Make Links And Buttons Recognized by Browser

I use [Vimium](https://vimium.github.io/) heavily. But Flutter rendering all the content into a canvas makes the clickable links and buttons not recognized by the browser, thus makes Vimium not working. This is a deal breaker for me, especially it's something I built that breaks my workflow.

I found a solution at the end to make links and buttons recognizable. It can be done by enabling semantics support. Add this line in the main function after `runApp`:

```dart
SemanticsBinding.instance.ensureSemantics();
```

This will render extra information in HTML instead of only drawing the canvas. It will make widgets like `Button` recognizable.

However, if you are using something more lower level like `GestureDetector`, you need to wrap the widgets with `Semantics`. Here is an example:

```
Semantics(button: true, enabled: true, child: myCustomClickable)
```

`myCustomClickable` will be recognized as a clickable element with that.


## Scrolling Behaviour

The scrolling feels choppy sometimes. And because the browser has no idea about the scroll position of the page, it just makes the scrolling behaviour feels different. For example, [here](https://github.com/flutter/flutter/issues/69529) is a Github issue opened 4 years ago describing this kind of problem and is still not resolved. For me, this is the last straw to make me give up Flutter, since it breaks scrolling keyboard shortcuts of Vimium.

## Conclusion

The idea behind Flutter is great. I hope the web support can be better and better so that I can finally come back to it one day. But for now, I cannot wait for it and need to take another route. Stay tuned for more updates about that journey.
