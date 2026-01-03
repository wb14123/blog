---
layout: post
title: Music Video Generation with AI
tags: ['AI', 'GenAI', 'Suno', 'ComfyUI', 'Wan']
index: ['/Computer Science/Machine Learning Application']
---

I did some music video generation experiments in November. Though lots of people are doing similar things nowadays and I'm not really doing anything unique, I got lots of joy and was kind of addicted to it for a few weeks. So I thought I'd record the experiments just to complete the missing piece of my blog posts in 2025, before I write the year end review blog.

## How It Started

It started when there were some AI generated music videos that became popular on a Chinese video platform Bilibili. The videos use characters in a popular classic Chinese novel [Journey to the West (西游记)](https://en.wikipedia.org/wiki/Journey_to_the_West), and make them sing songs in a recording studio. The results are very impressive. [Here](https://www.bilibili.com/video/BV1tVsHznELh) is an example of using Wukong (孙悟空) as the character.

That made me think: I like to write things. I write traditional Chinese [poems](/poetry) but it's hard to consume for average people and feels pedantic. I had always thought I could also write good lyrics, but they are useless without music. With examples of AI being so advanced to create really good music videos, I thought I'd give it a try. So this article is about the experiments I did, including the tools, models, GPU platforms and commercial products I explored. The end results are three music videos. Two of them I wrote the lyrics myself and one of them with ChatGPT generated lyrics. None of them got many views tho.

## Music Generation with Suno

[Suno](https://suno.com/) is probably the most popular music generation platform at the moment. I tried it when it first came out a few years ago. It was already very impressive back then, but I found it was not so good at pronouncing Chinese words (even though its ability to generate Chinese songs surprised me). I played with the free credits and stopped after I spent all of them. With the new wave of music videos on the Internet and from the comments under the videos, seems like its new V5 model is much better than older models. So I gave it another try. With the free credits ran out pretty quick, I subscribed for one month.

The model is much better at Chinese pronunciation, even though still far from perfect: it often reads some pretty common words wrong: about 90% of the songs it generated had at least one incorrect pronunciation. But it's useable and seems can be mitigated by using some other words/characters with the same pronunciation, which I never bothered to try.

You can control the song by adding some prompts in lyrics, enclosed by `[]`. Like the emotion, genre, instruments and so on. It needs a little bit prompt engineering, very much like image generation models in the early days. What I found really helpful is to use LLMs generate the prompts: I tell the LLM I want to generate music using Suno. I give it the lyrics and the goal I want. Then it can come up with some prompts with professional music terminologies. Then I try it in Suno, and if some parts are off, I come back and ask LLM again to change specific sections of the prompts. This feels like how people use the image generation models back in the day. I believe like those models, there will be more advanced music generation models with more natural prompting, like Nano Banana models nowadays.

Another thing I found helpful is to tune up the "Weirdness" in the advanced options, which can make the generated music less boring.

Even with all the tricks above, the quality of generated music is still like gambling. A slight change in prompts, lyrics or options can create very different result. I don't need the music to be really good since what I care more is the lyrics, but I still want it to express in the way I imagined while writing the lyrics. In order to achieve that, it needs lots of attempts. It's both frustration and addictive at the same time, which is also very much like gambling I guess. It's the most time consuming part (regarding human involvement) in the process.

If it's just for writing lyrics, music only is good enough for me. However, it's pretty boring to just share an audio to the Internet. I also wanted to explore the capability of video generation models nowadays. So my journey continued.

## Generate Longer Videos

Nearly all the models nowadays can only generate short videos less than 1 minute, and the quality tends to be worse the longer the video is. But a song is at least 2-3 minutes. So the trick is to combine short videos into longer ones.

One way to do that is to first create some images for key frames. Then prompt the model to start with a key frame and end at another one. Then combine the videos through editing. The results from this method can be a hit or miss: sometimes the content in the clips doesn't always match and it need more capable models and more attempts to get a good result.

This is where I found the cleverness in the videos I shared in the first section: the author generated the videos in a mostly static environment: a recording studio. So that it can avoid the situation of conflicting content in the clips. The whole video can be generated from only a single image, then merge the clips with some transition effect to smooth them out.

## Video Generation Models

From my research, [Wan](https://wan.video/research-and-open-source) based models seems to be the most popular open source model family. 2.1 and 2.2 seems to be very mature and lots of tools support them, while 2.5 is the newest version that supposed to have better result.

There is a model [InfiniteTalk](https://github.com/MeiGen-AI/InfiniteTalk). I believe it's based on Wan2.1 from the dependency models. It supports input of an image and a clip of audio, which is perfect for the music video use case when the character is mainly just singing in a recording studio.

## Video Generation Tools

While we have the models, they are mostly not user friendly to use, not to mention some of them just have model files. We need tools to run them.

The most popular one may be [ComfyUI](https://github.com/comfyanonymous/ComfyUI). Surprisingly, there is no official Docker image for it. There are a few third party ones but I don't really trust those, so I write [a wrapper](https://github.com/wb14123/comfy-docker) myself. The UI is very similar to the node editors in 3D software, which lets you edit the workflow pipeline by dragging the nodes and connecting them by arrows. It may looks familiar enough for people work more closely with graphic, 3D models or video editing, but I find this approach really hard to use. I'd rather write a few lines of Python code instead of dragging the nodes to if-else branches and for-loops. More importantly, it doesn't have a good dependency system: after importing a workspace and try to run it, there is not a good way to download all the models the workspace depends on, so it's hard to reproduce the workflow other people shared. So I just tried with some official work flow with Wan 2.2 model, which uses the Comfy Cloud service to run the model. The result is fine but the price is too expensive. Because my dislike of the UI, I gave it up at last.

The next tool I found is [Wan2GP](https://github.com/deepbeepmeep/Wan2GP). It has lots of built in models and workflows including Wan 2.2 and InfiniteTalk. It's much easier to use and requires less resource to run. The InfiniteTalk model it uses can theoretically generate infinite length videos by the method I talked in "Generate Longer Videos" above: it automatically use the last frames of the last clip to generate the next clip. But in order to generate longer videos, you need to tune the config in json file so that it allows you to generate longer clips on the UI.

## GPU Platforms

I have a GPU locally but the whole story is kind of sad: I built the current machine [back in 2016](/2016-06-19-Build-A-Computer-for-Deep-Learning.html) in the hope of doing some machine learning projects. And I did [train a model](https://github.com/wb14123/seq2seq-couplet) to play Chinese couplets which got some attention at the time. When I came to Canada, because of the incompatible power plugs, I bought a cheap adapter from Amazon. Unfortunately, the moment I plugged in my machine, there were sparks coming from the adapter and the machine was dead. After some debugging, I found the most expensive part of the machine, the GPU was dead. Since there was lots of other things to do after moving to Canada, I didn't feel the need to buy a replacement with a Nvidia 1050 at hand. Until pandemic hits and I wanted to do some machine learning projects again, and found GPU prices skyrocketed. After waiting and didn't see the hope of price dropping down, I bought a 3080Ti at a very high price in 2021, only to wait for ChatGPT and following open source models to release, and found out 3080Ti doesn't have enough memory to run a large enough model that is useful.

On the image generation side, it can run stable diffusion model with lower resolution. However, it's far from enough for video generation. I tried it with Wan2GP which already uses less resource than other tools, but it makes my whole machine freeze. So I need to find a cloud GPU platform.

[Runpod](https://www.runpod.io/) is a popular platform from my research, so I gave it a try. I did get Wan2PG to run on it but the product has lots of rough edges. Sometimes the remote SSH ports doesn't work. Sometimes the pods doesn't start successfully with a custom image because of stuck on Docker image downloading, and worse, it charges money when this happens. It's also not that cheap: ~$0.59/hour for a RTX 4090 pod. Wan2GP needs 1-2 hours to generate a 10-20 seconds video. So a music video can cost a few dollars.

I found some Chinese GPU platform cheaper in comparison. For example, [xiangongyun.com](https://www.xiangongyun.com/) provides RTX 4090 and RTX 4090D GPUs. Yes you heard it right: 4090D is a Nvidia GPU only targeted to Chinese market because of US government's ban on GPUs like 4090 in China. Ironically, the 4090D seems to be a better GPU for AI related tasks: it's less powerful but also cheaper and drains less power with the same amount of memory. There are even modded versions which doubled the memory from 24G to 48G. The price on Xiangongyun is like:

* ¥1.89/h (~$0.27) for 4090 24G
* ¥1.59/h (~$0.22) for 4090D 24G
* ¥2.59/h (~$0.37) for 4090D 48G

It's much cheaper than Runpod. But it doesn't support Docker images. Instead, it seems to target wider audience without software engineer background: it has a desktop environment like GUI, which lets you operation the pod and create custom images through that. Because of it's locating in China, you also need ways to resolve the problems caused by [GFW](https://en.wikipedia.org/wiki/Great_Firewall). For example, setting up HuggingFace proxy with something like `export HF_ENDPOINT=http://hf.x-gpu.com `. Unfortunately, I failed to install Wan2PG on it because of some shared Nvidia library issue: it seems it mounts some Nvidia library through Docker and I cannot change it or install another version. There are lots of third party images including ComfyUI with many models pre-installed, but I didn't feel it was trustworthy enough to run so I gave up at last.

Speaking of the GPU platform, I actually have built such a platform for a freelancer project around 2017. It uses Kubernetes under the hood, can create pods and allocate GPUs for the pods, supports custom Docker images and mounting file system for datasets, be able to view logs. It can also export service ports so that you can use things like Jupyter Notebook. From the experience of using the services above, I think the product I built back then was pretty advanced and I'm proud of it. Hopefully they can take good use of it.

## Commercial Video Generation Products

As noted above, the self hosted video generation with rented GPU is pretty expensive for generating a song: about a few dollars per song. So I also looked into some commercial providers. Lots of the providers say they have a free trail, but almost none of them can generate a video successfully without paying, including the official one from Wan. At last I found a Chinese provider. It's called [Jimeng (即梦)](https://jimeng.jianying.com/), which is created by ByteDance, the company behind TikTok. If not considering the new user promotion, the price is not attractive, basically comparable to renting a GPU. The new user promotion is less than one dollar for the first month subscription. It comes with some credits when first subscribed, then free credits everyday for a month. For the initial credits, you can create videos for about 2/3 song. Then about 1/3 song for the free credits everyday. That's at least a good price I can actually try to finish a music video, so I settled to it at last. It seems ironic that I settled with a commercial provider after so much efforts, but I'm glad I explored the possibilities.

## Overall Workflow

So summarizing the overall workflow to generate a music video with AI:

* Write Lyrics.
* Put it into Suno to try. Optional use LLM to write prompts for Suno. Try until satisfied.
* Break the songs into clips whose length can be supported by the video generation model. Make sure the cut point is nature, for example, not in the middle of the singing.
* Create an image for the video generate input. I use ChatGPT or Nano Banana for this.
* Input audio clips and the image to the video generation model to generate multiple video clips.
* Merge the generated videos and add transition effect in between. I use Kdenlive for editing the videos.
* Sometimes the videos generated is longer than the input audio with some blank part at the beginning and end. Use video editing software to sync the audio and video.

## Results

As stated above, the results are 3 Chinese music videos: [1](https://www.bilibili.com/video/BV1xp2uBHEP8), [2](https://www.bilibili.com/video/BV1DekSBhEAU) and [3](https://www.bilibili.com/video/BV1XBCtBGEgG). I think the results are good in general, other than some small things which I didn't bother to fix because of the cost:

* The model still cannot handle the hands properly. Sometimes there are 3 hands, some times there are 6 figures.
* The model still try to sync the lip even when there is only background music at the time.

I had lots of fun creating them. I don't know why but it was really addictive like video games. However, I couldn't justify the costs after the new user promotion was over so I gave up after that.

During my research, I also find the video and image generation communities are really similar to the early day video gaming and modding communities. Lots of people may not be professional programmers, but learn to use the models and tools with passion, and maybe learned some level of programming on the way. There is no things like Git for proper sharing and version tracking, just binary files and model files everywhere, shared through some sketchy cloud drive providers. The tutorials are everywhere: on video platforms like Youtube, in forums like Reddit and so on. It's a mess but so much fun.
