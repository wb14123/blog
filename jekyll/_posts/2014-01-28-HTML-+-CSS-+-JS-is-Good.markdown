---
layout: post
title: HTML + CSS + JS is Good
tags: [web, UI, frontend]
index: ['/Computer Science/UI/Javascript']
---

These days I'm using Libgdx to write a mobile game. While I'm writing it, I find that the modern web tech is very good: HTML + CSS + JS. Let me talk about the details.

The drawing part of the game is split into two parts:

* Init all the objects.
* Update these objects in every frame.

Why not do all the things in every frame? Because init objects need more time than change the states of an object in most cases. Init objects need to allocate memory, read configuration, load assets, and so on.

The init part also needs to specify the position of every object, or the layout of the stage. I write lots of code to init the position of every object. It is annoying. For example, when the width is changed, I have to change its position to make it centered or something like that. And the position specification is messed with the code. So it is better to make the position and layout data split from the code.

In web tech, here it is:

* HTML is what it has in the first place (init objects).
* CSS is the position and layout of the objects (data split from code).
* JS updates the objects when some events occur (update objects).

Everybody says and says that again. But I didn't understand that until I really wrote an application that render graphics to the screen.

So code is better than talk.
