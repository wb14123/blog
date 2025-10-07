---
layout: post
title: Download Message Images from Seesaw
tags: [life, seesaw, javascript]
index: ["/Computer Science/UI/Javascript"]
---

[Seesaw](https://seesaw.com/) is a learning experience platform for elementary education. My daughter's daycare uses it for communication with us. They also took pictures of her and send them through Seesaw every day. When my daughter transfers to a new daycare, I want to download all the photos before my account is deactivated. However, with its message history download tool, it can only download the messages as a PDF, and only has the first image's thumbnail if a message has multiple images. So I wrote a tool to download all the raw images. If anyone else needs it, [here](https://github.com/wb14123/seesaw-message-images-downloader) is the repo, which has all the source code and documentation.

The tool I wrote is just some Javascript code to run in the browser instead of an actual tool that takes some parameters and automatically downloads all the images. It's supposed to be thrown away after this one-time usage so I didn't put too much energy into making it clean and robust. Claude Code is good for writing things that don't need much maintenance. But it still needs a lot of guidance in this case because of the messy HTML it needs to parse. I did several iterations to make it be able to find the URL of raw images.

When running the code, it clicks on each message to open the image viewer, then clicks the next button to loop over the images to get the URL. I let it sleep 5 seconds after the clicks to wait for the elements to be fully loaded. So all the past photos are shown like slides when I run the code. It's such a special feeling to watch it running. From the early photos where she always has watery eyes because of crying, to eating lunch by herself, playing on the playground, and making all kinds of arts. It's hard to believe how much a toddler can grow in just more than half a year. And it's not only the kid who is growing. It feels like I also re-experienced the feelings when I was a kid, and learned to conquer those feelings again, and grow with her in the process.

Moving to a new place, my daughter goes to a new daycare, which has a pretty different principle and practice than the old one. Both she and I are adapting again. But I have more confidence because of the experience, and I believe she does, too.

