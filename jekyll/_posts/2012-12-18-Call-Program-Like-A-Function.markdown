
---
layout: post
title: Call Program Like A Function
tags: [shell, formal language]
---

What's the most important application for Linux users? The answer must be shell. It comes out the ability to combine tools together to solve complex problem. It is a little like function, such as `A | B` could respect as `A(B(x))`. But it is not so powerful. For example, how do you respect the format `A(B(x), C(y))` in a shell?

So here comes some libraries to call command like a function in programming languages. They are interesting but you may not want use them in a real project. Let's take a look first and then we will talk about them.

## [Sh](http://amoffat.github.com/sh/) (Python)

This library contains many advanced features and is well documented.

	# sort this directory by biggest file
	print(sort(du(glob("*"), "-sb"), "-rn"))
	
	# print(the number of folders and files in /etc
	print(wc(ls("/etc", "-1"), "-l")))

## [Shell](http://perldoc.perl.org/Shell.html) (Perl)

Using perl with shell command is always comfortable.

	use Shell qw(cat ps cp);
	
	$passwd = cat('</etc/passwd');
	@pslines = ps('-ww'),
	cp("/etc/passwd", "/tmp/passwd");
	# object oriented
	my $sh = Shell->new;
	print $sh->ls('-l');

## [Scsh](http://www.scsh.net/) (Scheme)

Scheme is a great and purl language. But scsh seems like a dead project. You can find [a fork on github](https://github.com/scheme/scsh). Howerver, I failed to compile its dependency [scheme48](http://s48.org/).

## [Shake](https://github.com/sunng87/shake.git) (Clojure)

I'm pretty proud to say it is written by one of my old colleagues. It has [a discuss on Hacker News](http://news.ycombinator.com/item?id=4553076).

	(require '[shake.static :as sh])
	
	;; any shell command ...
	(sh/uname -a) ;;returns a #<UNIXProcess java.lang.UNIXProcess@1833160>
	
	;; using clojure variables (vars, local bindings) in shake
	(let [home "/home/sunng87"]
	  (sh/ls -l $home))
	
	;; using clojure forms in shake
	(sh/curl $(format "https://github.com/%s" "sunng87"))

## Conclusion

So after all, you may ask: why not just use these language's REPL as a shell? It provides more power! But thinking of the daily life on Linux, how many times will you use these "advance features"? Instead, if you prefer the "function style shell", you need to type many parentheses, quotes and so on. I think the current ability of shell maybe not the most complete, but it is the most efficient way to work.
