---
layout: post
title: More About Program In Shell And Function
tags: [shell, formal language]
index: ['/Computer Science/Software Engineering']
---

Some months ago, I wrote a blog named "[Call Program Like A Function](/2012-12-18-Call-Program-Like-A-Function.html)". In that blog, I said using pipe in shell is like calling function: `A | B` is like `A(B())`. And I also said it's difficult to write in shell like `A(B(), C())`. Read this blog again today, I realize this thought is not totally right.

The thought above is based on this suppose: If we see program in shell like the function in programming, then the arguments of this "function" is standard input, the result of the "function" is standard output. There could only be one standard input, so the "function" must have one argument. In this way, there is no way to call program like `A(B(), C())`. But we could discuss these two points.

## Standard input as arguments

But think about the program could receive arguments from shell, we need not treat standard input as its argument. In C programming, it's like:

	int main(int argc, char *argv[])
	{
		return 0;
	}

`argc` is the number of arguments, and `argv` is what they are. So there is no need to see standard input as the program's arguments.

## Standard output as return value

Now let's think about why we treat program's standard output as function's return value. In the example above, you could return other type as you want, such as `void`, `char` and so on. But there will be a warning while compiling and the result will be transformed to `int` at last. You can try to modify the code like this:

	char main(int argc, char *argv[])
	{
		return 'a';
	}

Then compile and see the return value:

	~/testing » gcc -Wall a.c
	a.c:4:11: warning: return type of ‘main’ is not ‘int’ [-Wmain]
	------------------------------------------------------------
	~/testing » ./a.out
	------------------------------------------------------------
	~/testing » echo $?
	46

So if we want to get more information from program, we should get its output. Luckily, it's easy to do it in shell. Using `$()` you could get the output of program and using them in the shell. For example:

	echo `ls`

## And then?

So if we treat arguments of program as function's argument, standard output value as function's return value, then `A(B(), C())` could be written as <code>A `B` `C`</code>  in shell.

And there is a note. In C programming, the type you could return is almost the same with what you can return in program. But when you want to get a string's value in a function, you can use a string's pointer as its argument and assignment to it. But you can not do it in shell.

