---
layout: post
title: Build Erlang the Rebar Way
tags: [erlang, rebar]
---

These days I start learning erlang, and building a poker robot system. While I am learning it, I found the most difficult part is not the function style programming, nor the OTP system. The most difficult part is how to build and run erlang. Surely you can write an erlang module and run the functions from the erlang shell when you do exercises, but it is a little disturbing. And you surely don't want to do that in the production environment. The erlang way and reltool is a little difficult for the newbies. Thanks to rebar, we can do it much easier now.

[Rebar](https://github.com/basho/rebar) is a very good tool to build and run erlang applications. It could automatic get dependencies, run it as a daemon, attach it and hot load code. I'm wonder why there is no book about erlang introduced it. Even the book *Learn You Some Erlang for Great Good*, which is published this year. The rebar official wiki is a little simple. I will record how am I using rebar to build erlang applications.

Basic
----------------------

A typical erlang directory structure is like this:

	myapp:
	 - src/
	 - include/
	 - priv/
	 - ebin/

* src: place for source code.
* include: place for included files, such as `.hrl` files.
* priv: not used for me now.
* ebin: place for erlang object files, such as `.beam` files, `.app` file also placed here.

You usually need not to place any files in `ebin/` if you use rebar. All the object files, and `.app` files, could be compiled from sorce code. In a project, there may also have such two subdirectory: `deps` for dependencies and `test` for test files.

After download rebar, this command could create an OTP application:

	rebar create-app app-id=myapp

It will create a directory with a `src/` subdirectory, and a file named `rebar.config`. As the name specified, `rebar.config` is the erlang config file.

Use this command could build this app:

	rebar compile

Dependency
----------------------

Lots of languages could manage dependencies easily now. For example, gem in ruby, npm in node.js, pip in python, maven in java and so on. Erlang is an old language, but with rebar, you could manage dependencies easily.

Run `rebar help get-deps` to see how to add dependencies with rebar:

	{deps_dir,"deps"}
	{deps,[application_name,
	       {application_name,"1.0.*"},
	       {application_name,"1.0.*",
	                         {git,"git://github.com/rebar/rebar.git",
	                              {branch,"master"}}},
	       {application_name,[],
	                         {git,"git://github.com/rebar/rebar.git",
	                              {branch,"master"}},
	                         [raw]}]}

Use this command to automatic get dependencies:

	rebar get-deps

Test with Eunit
--------------------------

Rebar could also compile with test. I am just using eunit for now. I add these code in `rebar.config`:

	{cover_enabled, true}.
	{eunit_opts, [verbose, {report, {eunit_surefire, [{dir, "./"}]}}]}.

You can put test codes in subdirectory `test`. While run `rebar test`, rebar compile test code, put the object files in `.eunit`. It will also show test coverage in a web page.

Run the Application
---------------------------

Here comes the most important part. We will build and run our application. The rebar official wiki has an article about how to handle release, but it doesn't mention how to handle release with dependencies. I will introduce how I do it.

We already know we could use `rebar compile` to compile the codes. But it only put object files in `ebin/`, we also need to run them in the shell manually if we want:

	erl -pa ./ebin ./deps/*/ebin

This command will start the shell with compiled files, and you can run them in the shell.

But it is absolutely not we want. We want a executable file, just run it to start. Even better, run as a daemon. Even more better, attach it when we want.

Rebar allows us to do all test things. It is a little complex, but much simpler than the origin way with erlang.

First, make a new directory in the application directory, and using rebar to generate some files:

	mkdir rel
	cd rel
	rebar create-node nodeid=myapp

These command will create a subdirectory named `files` and a file named `reltool.config`. We need not to touch anything in the `files` directory. But we will modify something in `reltool.config`. I'm using erlang R16B01, and I do these things:

1. Add `{lib_dirs, ["../deps"]},` in the `sys` config. This will include our dependencies.
2. Change `{app, myapp, [{mod_cond, app}, {incl_cond, include}]}` to `{app, myapp, [{mod_cond, app}, {incl_cond, include}, {lib_dir, ".."}]}`.
3. Add `{sub_dirs, ["rel"]}.` in `rebar.config`.

Now, we could generate the executable files:

	rebar compile generate

This command will generate files in `rel/myapp`. We can run the app with erlang shell like this:

	./rel/myapp/bin/myapp/myapp console

Use `start` argument will start as a daemon, and using `attach` could come back to the erlang shell. You can see the usage with `help` argument.

OK, enjoy your self!

Reference
-------------------------

* Erlang application manual: just run `erl -man application`.
* [OTP design principles](http://www.erlang.org/doc/design_principles/users_guide.html).
* [Rebar official wiki](https://github.com/basho/rebar/wiki).

