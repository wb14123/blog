
---
layout: post
title: Beautiful Math with MathJex
tags: [composition, MathJex, LaTex]
---

Never ever say you are a geek if you don't have some mathematical formula on your website! I have done so though [MathJex](http://www.mathjax.org)`:)`. Here are some examples:

<span>$$ x = {-b \pm \sqrt{b^2-4ac} \over 2a} $$</span>

<span>$$ |s_n - s_m|=|\sum_{k=m+1}^n u_{k}|=|u_{m+1}+u_{m+2}+...+u_{n}| \lt \epsilon $$</span>

Now lets try some inline formulas, such as you may except <span>\(b \ne 0\)</span> in expression <span>\(a \over b\)</span> and the famous formula <span>\(E = mc^2\)</span> from Einstein.

However, there are some issues to use MathJex with Markdown because of the symbols in formula will be parsed by Markdown first. The easiest way to avoid this is surrounding your formula with `<span>` since Markdown will parse nothing in a html/xml block. Such as `<span>\(a \ne 0\)</span>` for a inline formula <span>\(a \ne 0\)</span>.
