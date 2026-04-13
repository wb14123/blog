---
layout: post
title: 'TCode: An AI Coding Agent Leverages Neovim and Tmux'
tags: [tcode, LLM, vim, tmux, linux, TUI, machine learning, AI]
index: ['/Computer Science/Machine Learning Application']
---

> If I have seen further, it is by standing on the shoulders of giants.
>
> -- Isaac Newton

In the last [year end review post](/2026-01-12-My-2025-in-Review.html), I mentioned I want to try coding agents other than Claude Code, since I want to avoid vendor lock-in for such an important tool in the age of AI coding. I said I would try some open source implementations like Open Code and migrate to them. The plan has been executed much better than I thought: just in the beginning of the new year, I implemented my own coding agent called [TCode](https://github.com/wb14123/tcode), and it's much more powerful than the tools like Claude Code and Open Code.


## Problems of Existing Tools

Other than the vendor lock-in problem, the existing coding agents also have other problems.

Since those tools are TUI based, I expect to use them in a familiar way with vim like keybindings. In this aspect, Claude Code is actually much better than Open Code, since it doesn't clean up the **main** conversation history in the output (main conversation means messages not including tool calls or subagent details), so that I can navigate the history with tmux. (But I find Claude Code seems to start cleaning up the conversation history in the output too sometimes, not sure if it's a bug or a feature). For Open Code, since it manages its own output and buffer, I must use its keybindings for basic operations like navigation through conversation, which doesn't support vim like keybindings and is very hard to use (at least for me).

Other than the keybindings, there is a trade off about how much detail to show about things like thinking tokens, tool calls, subagents, permissions and so on: showing too much, it's noisy; showing too little, the user doesn't know what is actually happening. Claude Code has some shortcuts like ctrl + o to expand some of the details, but I never find it easy to use. And even with that, the details are not enough and Claude Code is continuously showing less and less information saying it's a distraction for the user, which many users disagree with.

The last problem is transparency. Closed source projects make it hard to understand what has been changed. Even a simple prompt change can make a big difference for the result. Recently there are many reports of Claude Code performance degradation or cost increasing, and many of them are not because of the LLM model, but about the client changing its behavior. Also the quality of Claude Code doesn't seem to be very good: there are always small issues here and there, which are not deal breakers but annoying, and they are hard to fix because it's closed source.

With the problems existing in even open source tools, I decided to write my own tool, so that not only it fits my workflow, but I can also have a deeper understanding about how the LLM works. Luckily, I don't think it's too hard to implement one. In contrast, even though I think IDEs are important too, I wouldn't write one like IntelliJ IDEA, because it's just not worth the effort.

## How TCode Resolves the Problems

The last problem listed above can mostly be resolved by open sourcing, which I ended up doing for TCode. For the first two problems, TCode resolves them by leveraging existing tools: [Neovim](https://neovim.io/) and [tmux](https://github.com/tmux/tmux).

Let me give a brief introduction of these two dependencies first even though they are already very popular, just for the people who are not very familiar with them. Neovim is a drop in vim replacement with better defaults and easier plugin creation (critical to be used as a dependency). I never used it much before. But because of writing and using TCode, I started to use it and find it very pleasant, especially with projects like [LazyVim](https://www.lazyvim.org/) to give a good start on configuration.

Tmux is a "terminal multiplexer", which I still don't (bother to) know the exact meaning even after more than 10 years of using it. I'd like to think of it as a tiling window manager, but just for terminals: you can divide the terminal window vertically and horizontally, to whatever many levels you want. You can also create new tabs and sessions. All being able to navigation using convenient keybindings. It's the core tool of my terminal workflow. I cannot live without it: for example, it's one of the reasons I can tolerate MacOS. 

Before I start with TCode itself, let me show a screenshot of it, so it's easier to understand what I'm talking.

<img src="https://raw.githubusercontent.com/wb14123/tcode/refs/heads/master/assets/demo.gif" alt="demo"/>


### Writing and Showing Messages with Neovim

How does TCode leverage those 2 tools to resolve the problems above? Let's first talk about keybindings, focusing on the keybindings of message writing and showing first. The AI coding agent's interface is very similar to a chat application, but with much more complexity. For showing messages, it usually needs to navigate through the conversation, because the conversation messages can often be too long to fit in a single screen, and often have markdown styles and code in them. For editing messages, a good editor makes it easier to write better prompts: things like code snippet, more structured thoughts and so on.

Both problems can be resolved very well by vim, or Neovim in this case: vim supports so many highlight styles, convenient keybindings under normal mode to navigate, and itself is one of the most powerful editors. And better: it can all use the user's existing customization. So when adopting TCode, the user doesn't need to get used to the new style, new color scheme and keybindings, they can just use the existing ones.

So, TCode uses Neovim for showing and writing messages, which are the two panels on the left of the screenshot above.

In addition to it, Neovim is also used to preview diff, file writes and bash commands.

### Use Tmux to Manage Details

From the screenshot, you can see there are a few panels including the writing and showing messages ones we just talked about. All of them are in tmux, so you can just use keybindings to navigate through them. You can also customize the layout.

But even more powerful is how it resolves the problem of balancing the level of details: by default, the main conversation panel just shows the overview of the messages. For tool calls and subagents, it just shows a line to describe it and collapsed input. But if you want to see what is really happening in it, you can open the detailed view in a new tmux tab. For example, for the subagent, it has all the things like the main conversation: you can even interrupt it and send new messages to it. Those details are all available without making the main conversation's UI noisy, all thanks to the powerful tmux.

## Other Improvements

Other than the main features I talked about above involving Neovim and tmux, I also made lots of tweaks in TCode, because I can write it exactly as I want. Like improvement of the bash tool, shortcuts like `/plan`, `/review` that expand to detailed prompts in Neovim editing window, using a local Chrome browser so that it's able to use all the exiting accounts, and so on. There are just so many of them that I cannot list them all here. Read the [user docs](https://github.com/wb14123/tcode/tree/master/docs) if you are interested.

## Implementation Details

I also want to briefly talk about how I implemented it. Of course I implemented it using coding agents, Claude Code at the beginning, and TCode itself after the core features has been implemented. But for the core, I wrote the initial version of the llm-rs library by hand, which manages the conversation and events. This way I keep the core logic clean, and it shapes the whole architecture to what I want instead of complex prompting.

## Conclusion

So here it is, the coding agent I wrote, already one of my core tools that I use every day. I'm very happy I finished it to avoid vendor lock-in and be able to control the LLM behaviour more precisely. Hopefully other people will find it useful too. And hopefully there is a day in the near future that local LLMs can be powerful enough so that I can control the backend of it precisely as well.
