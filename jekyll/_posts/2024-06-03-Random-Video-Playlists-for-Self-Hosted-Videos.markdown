---
layout: post
title: Random Playlists for Self Hosted Videos
tags: [video, selfhost, vlc, kodi, media]
index: ["/Computer Science/Digital Life"]
---

With the development of computer systems and online streaming services, it's never been easier to play TV shows or movies on demand. There are some shows that I watch over and over when I want to relax. But the action of finding a show and selecting an episode makes it less casual. To some extent, I miss the old days to casually open a TV channel just to watch some random things. In this article, I will explain my journey to achieve that. More specifically, I want something like this:

* Be able to add videos into collections and play videos randomly from a collection.
* Be able to share the collections and videos to other devices including:
  + Other desktops and laptops, including Linux and MacOS.
  + Mobile devices including Android and iOS.
  + TVs like Android TV box.
* No transcoding on the server.
* The solution needs to be self hosted, free and open.

I think I need to add more details about "no transcoding on the server" since a lot of solutions need that. All my devices are compatible to play the formats in my video collection. So it's a waste of resources to do another transcoding on the server, especially since my video server is also the desktop PC I use the most every day. If this is not a requirement for you, you may be able to find much better solutions. That's why I listed everything I've tried so it may help someone even if it's not the solution I chose at last.

So here you go. If you just want to see my final solution, go to the last section.

## ErsatzTV

[ErsatzTV](https://ersatztv.org/) is a self hosted service to create live TV channels and stream them. You can add videos to collections, and put them into schedules. It works very much like a real TV channel. It has a Docker image and doesn't need any external databases, so it's really easy to try it out. Once you create channels it can generate m3u8 playlists so that you can stream it on any client that supports it.

It has great features. However, without transcoding on the server and use HLS Direct to stream the videos, there are some problems: I cannot open the stream in VLC. I could open it in Jellyfin, but once it jumps to another video with different format, it stops playing. I need to restart the client which is very annoying.

## Jellyfin

[Jellyfin](https://jellyfin.org/) is a very popular media server. It's like the more popular [Plex](https://www.plex.tv/) but is free and open source. I was never a fan of Plex since it just doesn't feel right to self host something you cannot really control. Jellyfin has gone a long way since I tried it a few years ago. You can add TV shows and movies to collections and play random videos from there. Even though it's not as powerful as the schedule feature in ErsatzTV, it's still great for my use case.

However, it falls short on "no transcoding on server" part again. The web client can only play a very limited video format. Its Linux client can play most video formats fairly well, but I still need my mobile devices be able to play the videos. The official Android and iOS clients are not any better than the web client. A third party iOS client [Swiftlin](https://github.com/jellyfin/Swiftfin) does much better, but somehow it cannot play from a collection.

## Kodi with Samba

I use Kodi on my Android TV box all the time. The videos are shared through Samba. Kodi has [an offical add-on](https://kodi.wiki/view/Add-on:Play_Random_Videos) to play random videos from TV shows, movies, folders and so on. However Kodi doesn't have the concept of collection. It has playlist but it's very hard to use. In theory you can symbol link all the videos to different folders as collections and play from there, but it's too hacky. Kodi has an iOS client but it's not in App Store so it needs to be compiled and resigned every a few days.

While I was exploring these ideas, I realized even Kodi doesn't have any good built in playlist or collection feature, there are some file formats for video playlists. With that, we can even use other video clients. m3u file came to mind at first but the videos paths should be relative paths so it can be played from any device even though the mount point is different. At last I found [XSPF](https://en.wikipedia.org/wiki/XML_Shareable_Playlist_Format) which allows relative path for the videos. With that, I came up with my final solution.

## XSPF Playlist on Samba with Kodi and VLC

So based on my exploration above, I came out an idea to use XSPF playlist to create collections. I just put the XSPF files in my videos folder to share through Samba together. Since the video paths are relative in the playlist, once you mount the Samba folder on other devices, you can just click the playlist and play it through supported clients.

For the clients, I use Kodi on the Android TV box and VLC on other devices. For Kodi, once [Play Random Videos add-on](https://kodi.wiki/view/Add-on:Play_Random_Videos) is installed, you can long press on the playlist file to play a random video. VLC on desktops [can be configured](https://superuser.com/a/1808182) to always play videos randomly from a playlist. On Android, there is an option to shuffle play once you open the playlist. But strangely, the VLC on my iPad is not able to play the XSPF file. I may dig into that in the future but it's good enough for me now.

The only part left is to create the XSPF playlist. It's a xml file so you can edit it manually but that takes too much time. So I created a Scala script to add or remove videos from a folder. Even though I used Scala for a long time and write my side projects with it, it's the first time I use it as scripting and it's such a pleasant with the help of [Ammonite](https://ammonite.io/). The script is on [Github](https://github.com/wb14123/xspf-editor) so that you can also use it if needed.
