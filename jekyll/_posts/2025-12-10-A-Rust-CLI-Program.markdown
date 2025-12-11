---
layout: post
title: A Rust CLI Program, Use It with LLM and Convert It to Web UI
tags: [Rust, web, CLI, WASM, LLM, AI, Claude]
index: ['/Computer Science/Programming Language/Rust']
---

In the [last blog post](/2025-10-28-My-First-Rust-Project.html), I talked about my first Rust project. Recently, I finished another Rust CLI project called [rhyme-checker](https://github.com/wb14123/rhyme-checker) that I wanted to build a long time ago. I tried it as a Claude Skill and got really good results. I also built a project [clap-web-gen](https://github.com/wb14123/clap-web-gen) to convert it (and be able to convert many other Rust CLI projects) to a web page running in the browser without any backend. This blog post is about the journey of both projects.

## A CLI Program to Check Rhymes for Chinese Poetry

There is a special kind of traditional Chinese poetry called "词". It literally means "word" or "lyrics". It's a kind of poetry that you need to follow strict rules. There are many titles. Each one has strict meter, rhyme schema and tonal patterns. The rule is strict because they actually came with music and you can sing them in ancient times. But unfortunately the music has been lost and all that's left are the lyrics.

I like to write such poetry because it's really fun, and with the strict meters, it sounds beautiful by default. There are many of them on [the poetry page](https://www.binwang.me/poetry). But it's hard to write because of the strict rules. I need to refer to the meters every time I write one. Sometimes there are just sentences coming out of my mind but I don't know which title fits them the best. So many years ago, I tried to write a program to match the best title based on the text you give it. I wrote it in Javascript, and also partly because I didn't understand the meter rules very well at the time, the project quickly became a disaster and I gave up at last. When I picked up Rust recently, I thought I could re-implement it with Rust as a CLI program. It's almost pure algorithm without much IO, which could benefit from the performance of Rust.

The algorithm of this program is quite challenging, since the rules of the poetry titles are complex. For example, here are some constraints:

* A title has a fixed number of sentences and each sentence has a fixed number of characters.
* Each Chinese character has a tone that can fall into two categories: 平 (Ping) or 仄 (Ze). In the meter rule, the character at each position must meet the requirement: it must be Ping, Ze, or either of them.
* Some characters at the end of sentences have requirements about the rhyme:
  * The rhyme also has tones of Ping and Ze. Characters with different tones are in different rhymes but can be in the same "rhyme group". And the rules have requirements about whether the rhymes need to be in the same rhyme group, or must not be in the same rhyme group.
  * There is not only one rhyme or rhyme group for a title: the rhyme and rhyme group can change from sentence to sentence.

So when searching for the best matching poetry title, it needs to loop over all the possible rhymes based on the input text, and try to put each line into different positions in different titles and see which one matches the best. I ended up using algorithms like depth-first search and dynamic programming for the searching. It may be the first time I used dynamic programming again after the programming contests in university. It feels great to use a language that you know what the cost of each operation is, like whether to create a new object for a variable or just use the reference to avoid copy and memory allocation, and so on.

The finished code is on [Github](https://github.com/wb14123/rhyme-checker). For a search with reasonably long input text, the time spent is consistently at about 80ms, and the memory usage peaked at about 5MB. I'm very happy with the result.

## Use the CLI Program as a Claude Skill

I tried to use AI for traditional Chinese literature a long time ago. I trained an RNN model about 10 years ago to [write Chinese couplets](https://ai.binwang.me/) and it gained a lot of interest at the time. With every new language model starting from GPT 3, I tried to use them to write traditional Chinese poems. They were very bad at first, but getting better and better especially since Deepseek was released. But they still make lots of mistakes especially about meters and rhymes, which play a very very important part in traditional Chinese poetry. Even when letting the LLM review a poem, such mistakes happen all the time.

I tried to train some LoRA with my own dataset. Also thought about using reinforcement learning with the meter matching as the score/cost. But because of the cost of training, I gave up both in the end.

However, there is a much cheaper option: let the existing LLM use external tools and use its reasoning ability to figure everything out. With the CLI written, which can query tones, rhymes, title rules and matching scores, we have such a tool. Claude Skill makes such tool usage very simple: just a markdown file to describe the tool and how to use it, along with the binary and that's all it needs. So I created [a markdown file](https://github.com/wb14123/rhyme-checker/blob/master/.claude/skills/rhyme-checker/SKILL.md) and tried it with Claude Code, and the result was really good! It can check the poem it wrote and fix it until it passes the checker. Sometimes it needs more time and can consume lots of tokens, but most of the time it can finish with a perfect matching score, while maintaining reasonably good content. Later I found out the Claude web UI also lets you upload a zip to create a new skill, so I did that and now I can use it anywhere.

The result makes me both excited and surprised. Traditional Chinese poetry is something that very few Chinese people can write nowadays. But maybe it's just a lack of interest which is not a problem for LLMs. After I created the skill, when I finish a conversation with Claude, I sometimes let it write a poem to summarize the conversation, and it's really entertaining.

## Convert Rust CLI to Web UI

I really like the CLI program I wrote. But sometimes I need to run it on mobile phones. So I wrote a program to convert any Rust CLI program to web UI, given the program can be compiled to WASM and uses [clap](https://docs.rs/clap/latest/clap/) for CLI args parsing. The code is also on [Github](https://github.com/wb14123/clap-web-gen). The usage is very simple: you just add the dependencies, put the main logic into a function that takes a clap structure as input, and add a macro onto it. Oh you also need to replace all the `print!` and `println!` since WASM cannot handle the stdout.

This is a fairly complex program as well since it uses macros and also needs to parse the structure and map them into HTML elements. But the complexity is different from the CLI tool I wrote: for the CLI tool, it's mostly the algorithm, but for this one, it's just more tedious once the approach was figured out. So I used Claude Code heavily in this project. I can check the result very easily and it runs purely on the client side, so there is less concern about not writing all the code by myself. It saved me lots of time both in the prototype and the implementation.

With my interest in Rust, I believe I will write more CLI programs and this tool will make it easier for me to run them everywhere and share them with other people. I'm looking forward to it!
