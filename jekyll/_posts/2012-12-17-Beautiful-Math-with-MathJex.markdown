---
layout: post
title: Beautiful Math with MathJax
tags: [composition, MathJax, LaTeX]
index: ['/Computer Science/UI/Javascript']
---

Never ever say you are a geek if you don't have some mathematical formulas on your website! I have done so through [MathJax](http://www.mathjax.org) :). Here are some examples:

Code:

	$$ x = {-b \pm \sqrt{b^2-4ac} \over 2a} $$
	$$ |s_n - s_m|=|\sum_{k=m+1}^n u_{k}|=|u_{m+1}+u_{m+2}+...+u_{n}| \lt \epsilon $$

Result:

<span>$$ x = {-b \pm \sqrt{b^2-4ac} \over 2a} $$</span>

<span>$$ |s_n - s_m|=|\sum_{k=m+1}^n u_{k}|=|u_{m+1}+u_{m+2}+...+u_{n}| \lt \epsilon $$</span>

Now let's try some inline formulas, such as you may expect <span>\(b \ne 0\)</span> in expression <span>\(a \over b\)</span> and the famous formula <span>\(E = mc^2\)</span> from Einstein.

However, there are some issues to use MathJax with Markdown because symbols in formula will be parsed by Markdown first. The easiest way to avoid this is surrounding your formula with `<span>` since Markdown will parse nothing in an html/xml block. Such as `<span>\(a \ne 0\)</span>` for an inline formula <span>\(a \ne 0\)</span>.
