---
layout: post
title: My Workflow to Review Articles with LLMs
tags: [AI, Machine Learning, LLM, blog]
index: ['/Computer Science/Machine Learning Application']
---

Since LLMs started to get popular, I find one of their strengths is to review things like code and writings. The strength is related to its weakness: it can hallucinate and just make up things but sound confident. However, when it reviews your own writings, you know the subject best, so if the LLMs are making up things, you'll catch that. After trying Claude Code at work, I found it useful and subscribed to the Pro version for personal use. With its CLI interface, it's really easy to write scripts to incorporate it into my workflow. In this article, I'll talk about how I use it to review my blog posts. It's very straightforward to use LLMs since you just interact with them using natural language, but I think there are some details worth sharing.

*As a side note, I created a new category "Machine Learning Application" and moved some of my old posts into this category, since I think just using LLMs with prompts wouldn't count as "Machine Learning."*

## Correctness Review

I uses a multi-round review process.

First, check correctness. If there is something technically wrong, a large part of the article will likely need to be reworked. Therefore, it's a waste of work to focus on minor details before addressing the fundamental issues.

The correctness check covers areas such as mathematics, algorithms, system designs and fact checks. I only want the LLM to tell me what is wrong so I can fix it myself instead of letting it do the fix for me. The prompt I use is on [my Github](https://github.com/wb14123/blog/blob/master/review/correctness-review.md). It may continue to evolve, but I'll copy what I have for now:

```markdown
# Guide for Correctness Check

* Check the technical correctness thoroughly. Think about all the scenarios.
* The technical correctness includes but not limited to these things:
  * Code example and syntax.
  * Algorithm descriptions.
  * Technical concepts and definitions.
  * System architecture designs and claims.
  * Mathematical formulas.
* Check facts in the articles. Search the internet if possible. Use reliable sources like reputable news organizations, official websites of the software mentioned and so on.
* Check the terminologies used in the articles are correct and used in a correct way.
* Check if there is confusing, not so clear description in the article.
* Break down into multiple sections if needed, and discuss them one by one with the user.
* All the links in the article without host name are paths under the domain `https://binwang.me`. You can fetch remote web pages if needed.
* Ignore grammar errors and the checks mentioned in @grammar-review.md.
* If there are correctness problems, respond with the problems in the chat instead of modifying the article files themselves.
```

I save the prompt into a file. One nice thing about Claude Code CLI is that you can use `@` to refer to local files. So I write a script to invoke it:

```bash
#!/bin/bash

set -e
set -x

file=$1

claude "Review @$file based on the review guide @correctness-review.md".
```

It may seem excessive to create a script for just one line of code. But it saves my time to write the same prompt repeatedly. It's also easier to swap `claude` to some other tool in the future if needed.

## Phrasing Review

The next stage is for phrasing and sentence flow review. It reviews issues such as awkward phrasing, unnatural sentences. This stage focuses on the text and wording itself instead of the logic or idea. I tried to let it review the structure as well, but its suggestions were so extensive that accepting them would make the article feel like it was written by someone else. I find reviewing the text itself strikes a good balance. Again, in this stage, I only let LLMs give me suggestions instead of editing the article by themselves. The prompt is on [my Github](https://github.com/wb14123/blog/blob/master/review/phrasing-review.md) too:

```markdown
# Guide for Phrasing and Sentence Structure Review

* Review the article for awkward phrasing, unnatural sentence flow, and syntax that sounds weird or unsmooth to native speakers.
* Focus on sentence structure, word choice, and natural language flow rather than grammar rules (checked in @grammar-review.md) or technical correctness (checked in @correctness-review.md).
* Make sure phrases and expressions sound natural and idiomatic in English.
* DO NOT change the file.
* Discuss every found issue with the user:
  * Describe the issue with reference to the text.
  * Give suggestions for the fix.
  * Discuss 3-5 issues at a time if there are too many issues.
```

And the prompt is wrapped in a similar script as before.

Sometimes I find it still tries to find lots of grammar errors even when I tell it not to do so. If that happens, maybe try to do the next stage of grammar review and then come back.

## Grammar Review

Finally, I let LLMs review the grammar, spelling and other typos. In this stage, I trust the LLM enough to make it actually fix the issues. But instead of editing it in place, I let it create a new file. In theory, I could `git add` or `git commit` the files so that I can review the diff but sometimes it can be forgotten, so creating a new file is the safest way.


The prompt is also on [my Github](https://github.com/wb14123/blog/blob/master/review/grammar-review.md) but I find something like "Fix the typos in [article]" is good enough most of the time.

Again, a similar script is created to wrap the prompt. The difference is I use vimdiff to open the diff at the end of the script.

## Diff Tool

I use diff to review the changes LLM made for the grammar review. But there are some changes needed to make life easier.

The first thing is to make it highlight the diff at word level. There is a plugin [diffchar](https://github.com/rickhowe/diffchar.vim) to do that.

Then I changed some default colors for vim highlight:

```
highlight DiffAdd    ctermbg=22  ctermfg=white guibg=#003300 guifg=white
highlight DiffChange ctermbg=16 ctermfg=white guibg=#001c65 guifg=white
highlight DiffDelete ctermbg=52  ctermfg=red   guibg=#330000 guifg=#ff6666
highlight DiffText   ctermbg=53  ctermfg=white guibg=#330033 guifg=white
```

Also set up the line wrap and read only mode (for `git difftool`):

```
autocmd VimEnter * if &diff
autocmd VimEnter *   windo set wrap
autocmd VimEnter *   windo set noreadonly
autocmd VimEnter * endif
```

## Fix All the Typos in the Past

Since I find Claude does a good job fixing the grammars in the articles, I wrote a script to fix all the issues in my past blogs:

```bash
#!/bin/bash

set -e

START_FILE="$1"

for f in `ls -r jekyll/_posts` ; do
	if [ -n "$START_FILE" ] && [[ "$f" > "$START_FILE" ]]; then
		echo "Skip $f since it's not after $START_FILE"
		continue
	fi

	p="jekyll/_posts/$f"
	echo "Fixing $f ..."
	claude -p "Fix typos in @$p . Never change the file name, links in the article. Skip and exit if it's not an English article."
done
```

You may notice that I don't like Claude Code to figure out all the typos in one session, but create separate sessions for each article. Since Claude will take the shortcut and not actually do a good job if I ask it to fix all at once. Be aware fixing them one by one will use lots of tokens. But I just leave it running overnight for a few days so the quota will be recovered during the day time.

If there are many articles, the quota will be all consumed without fixing all of them yet. So I take a parameter for the script to let it know the current progress.

This is a one-liner to review the changed files with `git difftool`:

```bash
f=`git diff --name-only | tail -1` ; git difftool $f && git add $f
```

It will open a file that's not staged yet with the configured git difftool (vimdiff in my case). Once you review it and close the difftool, it will use `git add` to stage it so the next file opened will be a different one.

I think it really did a good job fixing my articles. [Here](https://github.com/wb14123/blog/commit/05af92e6efb310f5600d6704bb8be87ea9d0d893) is one example of the running batch.

## Mental Model

I want to emphasize the mental model I use when having LLMs review my articles: I treat it as the last line of defense. I only make the LLM review the article once I think it's good myself and want an additional eye on it. And only make it fix grammars by itself. And even for the grammars, I'll review the result so that I can learn from the errors.
